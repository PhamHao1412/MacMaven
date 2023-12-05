package WebProject.WebProject.service.impl;

import WebProject.WebProject.entity.CustomUserDetail;
import WebProject.WebProject.entity.User;
import WebProject.WebProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(email);
        if (user == null)
            throw new UsernameNotFoundException("User không tìm thấy");
        return new CustomUserDetail(user);
    }
    public CustomUserDetail loadUserByUserId(String id) throws UsernameNotFoundException {
        User user = userRepository.findByIdAndRole(id,"user");
//        User user = userRepository.findById(id);
        if (user == null)
            throw new UsernameNotFoundException("User không tìm thấy");
        return new CustomUserDetail(user);
    }
}