package WebProject.WebProject.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import WebProject.WebProject.config.JwtTokenProvider;
import WebProject.WebProject.entity.*;
import WebProject.WebProject.model.GoogleUtils;
import WebProject.WebProject.service.impl.CustomUserDetailService;
import filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import WebProject.WebProject.service.CloudinaryService;

import WebProject.WebProject.model.Mail;
import WebProject.WebProject.service.CartService;
import WebProject.WebProject.service.CookieService;
import WebProject.WebProject.service.MailService;
import WebProject.WebProject.service.UserService;

@Controller
//@RestController
public class UserController {
	@Autowired
	UserService userService;

	@Autowired
	CartService cartService;

	@Autowired
	MailService mailService;

	@Autowired
	CloudinaryService cloudinaryService;
	@Autowired
	HttpSession session;

	@Autowired
	CookieService cookie;
	@Autowired
	GoogleUtils googleUtils;

	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	JwtAuthenticationFilter jwtAuthenticationFilter;
	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	CustomUserDetailService customUserDetail;
	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;


	@GetMapping("/signin")
	public String SigInView(Model model) throws Exception {
		return "signin";
	}

	@GetMapping("/signup")
	public String SignUpView(Model model) {
		return "signup";
	}

	@GetMapping("/contact")
	public String ContactView(Model model) {
		return "contact";
	}

	@GetMapping("about")
	public String AboutView(Model model) {
		return "about";
	}

	@GetMapping("blog")
	public String BlogView(Model model) {
		return "blog";
	}
	@GetMapping("/api/login")
	public ResponseEntity<User> getUserInfo(HttpServletRequest request) {
		String jwt = jwtAuthenticationFilter.getJwtFromRequest(request);
		if (jwt != null && tokenProvider.validateToken(jwt)) {
			String userId = tokenProvider.getUserIdFromJWT(jwt);
			User user = userService.findByIdAndRole(userId,"user");
			if (user != null) {
//				AuthUser authUser = user.toDTO();
				return ResponseEntity.ok(user);
			}
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	@PostMapping("/api/login")
	public ResponseEntity<AuthResponse> authenticateUser(@RequestBody AuthRequest request) {
		try {
				Authentication authentication = authenticationManager.authenticate(
						new UsernamePasswordAuthenticationToken(
								request.getUsername(),
								request.getPassword()
						)
				);
			CustomUserDetail customUserDetail = (CustomUserDetail) authentication.getPrincipal();
				String jwt = tokenProvider.generateToken(customUserDetail);
			AuthResponse response = new AuthResponse(customUserDetail.getId(),jwt);
			return ResponseEntity.ok().body(response);
		} catch (BadCredentialsException ex) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}
	@PostMapping("/signin")
	public String SignIn(@ModelAttribute("login-name") String loginname, @ModelAttribute("password") String password,
						 @RequestParam(value = "remember", defaultValue = "false") boolean remember, Model model) throws Exception {
		User user = userService.findByIdAndRole(loginname, "user");
		if (user != null) {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							loginname,
							password
					)
			);
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (passwordEncoder.matches(password,user.getPassword())) {
				String jwt = tokenProvider.generateToken((CustomUserDetail)authentication.getPrincipal());
					if (remember == true) {
						cookie.create("JWT", jwt, 3);
						cookie.create("remember", "remember", 3);
					}
					session.setAttribute("acc", user);
					List<Cart> listCart = cartService.GetAllCartByUser_id(user.getId());
					session.setAttribute("countCart", listCart.size());
				return "redirect:/home";
			} else {
				model.addAttribute("errorLogin", "Tên đăng nhập hoặc mật khẩu không chính xác!");
				return "signin";
			}
		} else {
			model.addAttribute("errorLogin", "Tên đăng nhập hoặc mật khẩu không chính xác!");
			return "signin";
		}
	}
	@PostMapping("/signup")
	public String SignUp(@ModelAttribute("username") String id, @ModelAttribute("your_email") String email,
						 @ModelAttribute("fullname") String fullname, @ModelAttribute("password") String password,
						 @ModelAttribute("comfirm_password") String comfirm_password, Model model) throws Exception {

		User user = userService.findByIdAndRole(id, "user");
		if (user == null) {
			String encodedValue = new BCryptPasswordEncoder().encode(password);
			String avatar = "https://haycafe.vn/wp-content/uploads/2022/02/Avatar-trang-den.png";
			User newUser = new User(id, "default", "user", encodedValue, fullname, avatar, email, null, null);
			userService.saveUser(newUser);
			return "redirect:/signin";
		} else {
			model.addAttribute("errorSignUp", "Tài khoản đã tồn tại!");
			return "signup";
		}
	}
	@GetMapping("/signout")
	public String SignOut(Model model) {
		session.setAttribute("acc", null);
		cookie.delete("remember");
		cookie.delete("token");
		cookie.delete("tokenGoogle");
		return "redirect:/home";
	}

	@GetMapping("/myprofile")
	public String Myprofile(Model model, HttpServletRequest request) {
		User user = (User) session.getAttribute("acc");
		String referer = request.getHeader("Referer");
		String messageChangeProfile = (String) session.getAttribute("messageChangeProfile");
		model.addAttribute("messageChangeProfile", messageChangeProfile);
		session.setAttribute("messageChangeProfile", null);
		if (user == null) {
			return "redirect:" + referer;
		} else {
			String error_change_pass = (String) session.getAttribute("error_change_pass");
			String ChangePassSuccess = (String) session.getAttribute("ChangePassSuccess");
			model.addAttribute("error_change_pass", error_change_pass);
			model.addAttribute("ChangePassSuccess", ChangePassSuccess);
			session.setAttribute("error_change_pass", null);
			session.setAttribute("ChangePassSuccess", null);
			model.addAttribute("user", user);
			return "myprofile";
		}

	}
	@PostMapping("/changepassword")
	public String ChangePassword( @RequestParam("newPassword") String newPassword,
								  @RequestParam("confirmPassword") String confirmPassword,
								  @RequestParam(value = "oldPassword", required = false) String oldPassword, HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		User user = (User) session.getAttribute("acc");
//		String decodedValue = new String(Base64.getDecoder().decode(user.getPassword()));
		if (oldPassword != null && !new BCryptPasswordEncoder().matches(oldPassword, user.getPassword())) {
			session.setAttribute("error_change_pass", "Current Password does not match!");
			return "redirect:/myprofile";
		}
		if (!newPassword.equals(confirmPassword)) {
			session.setAttribute("error_change_pass", "New password and confirm password do not match!");
			return "redirect:/myprofile";
		}
		user.setPassword(new
				BCryptPasswordEncoder().encode(newPassword));
		userService.saveUser(user);
		session.setAttribute("acc", user);
		session.setAttribute("ChangePassSuccess", "ChangePassSuccess");
		return "redirect:" + referer;
	}
	@PostMapping("/changeProfile")
	public String ChangeProfile(Model model, @ModelAttribute("avatar") MultipartFile avatar,
								@ModelAttribute("fullname") String fullname, @ModelAttribute("phone") String phone,
								@ModelAttribute("email") String email) throws IOException {
		User user = (User) session.getAttribute("acc");
		if (user != null) {
			if (!avatar.isEmpty()) {
				String url = cloudinaryService.uploadFile(avatar);
				user.setAvatar(url);
			}
			user.setUser_Name(fullname);
			user.setEmail(email);
			user.setPhone_Number(phone);
			userService.saveUser(user);
			session.setAttribute("acc", user);
			session.setAttribute("messageChangeProfile", "Change Success.");
			return "redirect:/myprofile";
		} else {
			return "rediect:/home";
		}
	}

	@GetMapping("/forgot")
	public String forGotView(Model model) {
		String error_forgot = (String) session.getAttribute("error_forgot");
		model.addAttribute("error_forgot", error_forgot);
		session.setAttribute("error_forgot", null);
		model.addAttribute("forgot", "Forgot Password");
		return "signin";
	}

	@PostMapping("/forgot")
	public String forGotHandel(@ModelAttribute("login-name") String login_name, Model model) throws Exception {
		User user = userService.findByIdAndRole(login_name, "user");
		if (user == null) {
			session.setAttribute("error_forgot", "UserName is not correct!");
			return "redirect:/forgot";
		} else {
			session.setAttribute("userForgot", user);
			return "redirect:/code";
		}
	}
	@GetMapping("/code")
	public String codeView(Model model) throws Exception {
		User userForgot = (User) session.getAttribute("userForgot");
		String noSendEmail = (String) session.getAttribute("noSendEmail");
		if (noSendEmail == null) {
			int code = (int) Math.floor(((Math.random() * 899999) + 100000));
			Mail mail = new Mail();
			mail.setMailFrom("haovo1512@gmail.com");
			mail.setMailTo(userForgot.getEmail());
			mail.setMailSubject("For got Password");
			mail.setMailContent("Your code is: " + code);
			mailService.sendEmail(mail);
			System.out.println(code);
			session.setAttribute("code", code);
		}
		session.setAttribute("noSendEmail", null);
		String error_code = (String) session.getAttribute("error_code");
		model.addAttribute("error_code", error_code);
		session.setAttribute("error_code", null);
		model.addAttribute("forgot", "Forgot Password");
		model.addAttribute("sendcode", "sendcode");
		return "signin";
	}

	@PostMapping("/code")
	public String codeHandel(@ModelAttribute("code_input") int code_input, Model model) throws Exception {
		int code = (int) session.getAttribute("code");
		if (code == code_input) {
			session.setAttribute("code", null);
			return "redirect:/newpass";
		} else {
			session.setAttribute("noSendEmail", "noSendEmail");
			session.setAttribute("error_code", "Code is not correct!");
			return "redirect:/code";
		}

	}
	@GetMapping("/newpass")
	public String newPassView(Model model) {
		String error_newpass = (String) session.getAttribute("error_newpass");
		session.setAttribute("error_newpass", null);
		model.addAttribute("error_newpass", error_newpass);
		model.addAttribute("forgot", "Forgot Password");
		model.addAttribute("sendcode", "sendcode");
		model.addAttribute("changepass", "changepass");
		return "signin";
	}

	@PostMapping("newpass")
	public String newPassHandel(@ModelAttribute("new_pass") String new_pass,
								@ModelAttribute("confirm_new_pass") String confirm_new_pass, Model model) throws Exception {
		if (new_pass.equals(confirm_new_pass)) {
			String encodedValue = Base64.getEncoder().encodeToString(new_pass.getBytes());
			User userForgot = (User) session.getAttribute("userForgot");
			userForgot.setPassword(encodedValue);
			userService.saveUser(userForgot);
			return "redirect:/signin";
		} else {
			session.setAttribute("error_newpass", "Confirm New Password not valid!");
			return "redirect:/newpass";
		}

	}
	@GetMapping("/signin-google")
	public String LoginGoogle(@AuthenticationPrincipal OAuth2User principal,OAuth2AuthenticationToken authentication) {
		String id = (String) principal.getAttribute("sub");
		String email = principal.getAttribute("email");
		String name = principal.getAttribute("name");
		String avatar = (String) principal.getAttribute("picture");
//		OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
//				authentication.getAuthorizedClientRegistrationId(),
//				authentication.getName()
//		);
//		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		User user = userService.findByIdAndRole(id,"user");
		if (user == null) {
			user = new User(id, "google", "user", null, name, avatar, email, null, null);

		}else{
			user.setAvatar(avatar);
		}
		userService.saveUser(user);

		cookie.create("IdToken", id, 3);
		cookie.create("remember", "remember", 3);
		return "redirect:/home";
	}
}
