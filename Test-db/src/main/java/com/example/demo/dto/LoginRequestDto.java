package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;

//회원가입 데이터 전달 DTO	
@Data
public class LoginRequestDto {
	private String username;
	private String password;
	private String roles;
	
	
	
	
	
	
}
