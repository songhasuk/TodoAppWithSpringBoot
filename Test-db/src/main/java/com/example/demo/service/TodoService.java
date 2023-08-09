package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.TodoDAO;
import com.example.demo.dto.TodoDTO;

@Service
public class TodoService {
	
	
	@Autowired
	private TodoDAO dao;
	
	public void insertdb(TodoDTO todos) {
		
		try {
			dao.insertTodo(todos);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public List<TodoDTO> selectAll(){
		List<TodoDTO> list = dao.selectall();
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
				
		return list;
	}
	
	
	public void delectTodo(String id) {
		
		dao.delectTodo(id);
	}
	
	public void updateTodo(Map<String, String> map ) {
		dao.updateTodo(map);
	}

}
