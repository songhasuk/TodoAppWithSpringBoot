package com.example.demo.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.TodoDTO;

@Mapper
public interface TodoDAO {
	
	public void insertTodo(TodoDTO todos);
	public List<TodoDTO> selectall();
	public void delectTodo(String id);
	public void updateTodo(Map<String, String> map );
}
