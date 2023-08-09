package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UsersDTO;
import com.example.demo.repository.UserRepository;




@RestController
@RequestMapping("api/v1")
@CrossOrigin ("http://localhost:3000")
public class MemberController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;
	
	
	@PostMapping("join")
	public String join(@RequestBody UsersDTO user) {
	
		user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
		user.setRoles("ROLE_USER"); //사용자 권한 데이를 세팅
		userRepository.save(user);
		return "회원가입완료";
	}

}
