package com.example.demo.dao;

import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UsersDTO;

@Mapper
public interface MemberDAO {
	
	public void insertMember(UsersDTO users);
	public UsersDTO selectUser(String username);
}
