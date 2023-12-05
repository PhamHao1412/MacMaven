package WebProject.WebProject.config;

import WebProject.WebProject.entity.CustomUserDetail;
import WebProject.WebProject.entity.User;
import WebProject.WebProject.entity.UserInfoResponse;
import WebProject.WebProject.service.UserService;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${google.user-info-endpoint}")
    private String GOOGLE_USER_INFO_ENDPOINT;

    @Value("${app.jwt.secret}")
    private  String JWT_SECRET;
    @Autowired
    UserService userService;
    private final long JWT_EXPIRATION = Duration.ofDays(3).toMillis();
    //Get user form Google access token gg
    public  String getUserIdFromGAT(String accessToken) {
        // Thực hiện HTTP request để lấy thông tin người dùng từ Google API
        String userInfoUrl = GOOGLE_USER_INFO_ENDPOINT + "?access_token=" + accessToken;
        RestTemplate restTemplate = new RestTemplate();
        UserInfoResponse userInfoResponse = restTemplate.getForObject(userInfoUrl, UserInfoResponse.class);
        if (userInfoResponse != null) {
            User user = userService.getUserByEmail(userInfoResponse.getEmail());
            return user.getId();
        } else {
            return null;

        }
    }
    // Tạo ra jwt từ thông tin user
        public String generateToken(CustomUserDetail userDetails) {
            Date now = new Date();
            Date expiryDate = new Date(System.currentTimeMillis() + JWT_EXPIRATION);
            // Tạo chuỗi json web token từ id của user.
            return Jwts.builder()
                    .setSubject(userDetails.getId())
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                    .compact();
        }


    // Lấy thông tin user từ jwt
    public String getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(JWT_SECRET)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(JWT_SECRET).parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }
}
