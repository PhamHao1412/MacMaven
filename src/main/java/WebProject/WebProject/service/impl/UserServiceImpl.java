package WebProject.WebProject.service.impl;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import WebProject.WebProject.entity.User;
import WebProject.WebProject.repository.UserRepository;
import WebProject.WebProject.service.UserService;

@Service
public class UserServiceImpl implements UserService{

	@Autowired
	SessionFactory factory;
	
	private UserRepository userRepository;
	public UserServiceImpl(UserRepository userRepository) {
		super();
		this.userRepository=userRepository;
	}
	@Override
	public List<User> getAllUser() {
		// TODO Auto-generated method stub
		return userRepository.findAll();
	}

	@Override
	public User saveUser(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public User updateUser(User user) {
		// TODO Auto-generated method stub
		return userRepository.save(user);
	}

	@Override
	public void deleteUserById(String id) {
		// TODO Auto-generated method stub
		userRepository.deleteById(id);
	}
	@Override
	public User getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return userRepository.findByEmail(email);
	}
	@Override
	public User findByIdAndRole(String id, String role) {
		return userRepository.findByIdAndRole(id, role);
	}
	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}
	public void processOAuthPostLogin(String username,String email){
		User existUser = userRepository.findByEmail(email);
		if (existUser == null) {
			String avatar = "https://haycafe.vn/wp-content/uploads/2022/02/Avatar-trang-den.png";
			User newUser = new User(email, "Google", "user", null, username, avatar,email , null, null);
			userRepository.save(newUser);
		}
	}

	@Override
	public User findByUserName(String username) {
		return userRepository.findByUsername(username);
	}
}
