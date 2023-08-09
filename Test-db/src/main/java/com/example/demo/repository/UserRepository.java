package com.example.demo.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.demo.dao.MemberDAO;
import com.example.demo.dto.UsersDTO;


//★☆ DB연동을 위해 사용되는 클래스를 Map으로 모델링한 것임  
//클라이언트에서 전달된 데이터가 이 UserRepository로 Map에 저장해서 db에 전달함 
//@Mapper로 바뀜 interface
@Service
public class UserRepository  {
	private Map<String, UsersDTO> usersMap = new HashMap<>();
	
	@Autowired
	private MemberDAO dao;
	
	//발급된 토큰 문자열에서 => username데이터를 얻고 
	//해당 username를 이용해서 [사용자 객체]를 얻고 싶을 때 => findByUsername 사용
	public UsersDTO findByUsername(String username) {
		System.out.println("sdfdsovndslvnlksd");
		UsersDTO dto =  dao.selectUser(username);
		System.out.println("dfd"+dto);
		return dto;
	}
	
	//회원가입 시 사용되는 함수
	public void save(UsersDTO users) {
		
		try {
			dao.insertMember(users);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	
	}
	
	//관리자가 전체 사용자 목록을 조회하기 위해 사용하는 함수
	public List<UsersDTO> findAll() {
		return new ArrayList<>(usersMap.values());
	}
}
