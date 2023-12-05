package WebProject.WebProject.service;

import java.util.List;

import WebProject.WebProject.entity.User;
import org.springframework.stereotype.Repository;


public interface UserService {
	List<User> getAllUser();

	User saveUser(User user);

//	User getUserById(String loginname);

	User updateUser(User user);

	void deleteUserById(String id);
	
	User getUserByEmail(String email);

	User findByIdAndRole(String id, String role);
	User findByUserName(String id);

	List<User> findAll();
	public void processOAuthPostLogin(String username,String email);
}
