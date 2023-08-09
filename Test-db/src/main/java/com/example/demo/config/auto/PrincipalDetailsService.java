package com.example.demo.config.auto;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.dao.MemberDAO;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UsersDTO;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

	private final UserRepository userRepository;
	


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("PrincipalDetailsService : 진입"+username);
		UsersDTO user = userRepository.findByUsername(username);
		System.out.println(user);
		//세션을 사용할때는 아래 코드 사용하여 추가함s 
		// session.setAttribute("loginUser", user);
		return new PrincipalDetails(user);
	}
}
