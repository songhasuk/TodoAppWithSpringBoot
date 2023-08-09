package com.example.demo.controller;

import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TodoDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TodoService;



@RestController
@RequestMapping("/api/v1/user/")
public class TodoController {

	@Autowired
	private TodoService todoService;
	
	

	@PostMapping("/insertdb")
	public ResponseEntity<?> insertdb(@RequestBody TodoDTO todos) {
		String message = "성공";
		System.out.println(todos.toString());
		todoService.insertdb(todos);

		System.out.println("컨트롤러");
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

	@GetMapping("/selectall")
	public ResponseEntity<?> selectAll(@RequestHeader("Authorization") String authorizationHeader) {
	
		String message = "성공";

		System.out.println(todoService.selectAll());
		return new ResponseEntity<>(todoService.selectAll(), HttpStatus.OK);
	}

	@DeleteMapping("deletetodo/{id}")
	public ResponseEntity<?> deleteTodo(@PathVariable String id) {
		String message = "성공";
		System.out.println("컨트롤러"+id);
		todoService.delectTodo(id);
		return new ResponseEntity<>(message, HttpStatus.OK);
	}
	@PutMapping("updatetodo/{id}/{checked}")
	public ResponseEntity<?> updateTodo(@PathVariable String id, @PathVariable String checked) {
		String message = "성공";
		System.out.println(checked);
		Map<String, String> map = new HashMap<>();
		map.put("id", id);
		map.put("checked", checked);
		
		todoService.updateTodo(map);
		
		return new ResponseEntity<>(message, HttpStatus.OK);
	}

}