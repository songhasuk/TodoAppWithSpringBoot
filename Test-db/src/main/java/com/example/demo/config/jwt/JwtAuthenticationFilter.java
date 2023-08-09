package com.example.demo.config.jwt;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.apache.catalina.startup.UserDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.config.auto.PrincipalDetails;
import com.example.demo.config.auto.PrincipalDetailsService;
import com.example.demo.dao.MemberDAO;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UsersDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

//JwtAuthenticationFilter에 대한 생성자 함수를 만들어서 
//final로 멤버 필드로 정의된 AuthenticationManager객체의 자동 생성할 수 있게 하는것
//로그인 객체로 데이터를 가져와 => 인증 작업을 처리하여 토큰을 발급해준다.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

	//스프링 쪽에서 FilterChain객체를 먼저 생성하기 때문에 => 
	//SecurityConfig에서 정의된 객체를 가져옴(생성) => 로그인 정보를 가져올 수 있는 객체다

	private final AuthenticationManager authenticationManager;

	

	
	// Authentication 객체 만들어서 리턴 => 의존 : AuthenticationManager
	// 인증 요청시에 실행되는 URL => /login  
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		
		System.out.println("JwtAuthenticationFilter : 진입");
		
		// request에 있는 username과 password를 파싱해서 자바 Object로 받기
		//ObjectMapper : 오브젝트를 객체로 변화해준다.
		ObjectMapper om = new ObjectMapper();
		LoginRequestDto loginRequestDto = null;
		try {
			
			loginRequestDto = om.readValue(request.getInputStream(), LoginRequestDto.class);
			
			//loginRequestDto = detailsService.loadUserByUsername()
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("JwtAuthenticationFilter : "+loginRequestDto);
		
		// 유저네임패스워드 토큰 생성
		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						loginRequestDto.getUsername(), 
						loginRequestDto.getPassword());
		
		System.out.println("JwtAuthenticationFilter : 토큰생성완료");
		
		// authenticate() 함수가 호출 되면 인증 프로바이더가 PrincipalDetailsService의
		// loadUserByUsername(토큰의 첫번째 파라메터) 를 호출하고
		// UserDetails를 리턴받아서 토큰의 두번째 파라메터(credential)과
		// UserDetails(DB값)의 getPassword()함수로 비교해서 동일하면
		// Authentication 객체를 만들어서 필터체인으로 리턴해준다.
		
		// 인증 프로바이더의 디폴트 서비스는 UserDetailsService 타입
		// 인증 프로바이더의 디폴트 암호화 방식은 BCryptPasswordEncoder
		// 결론은 인증 프로바이더에게 알려줄 필요가 없음.
		// 발급된 토큰 데이터를 유저 데이터에 전달
		Authentication authentication = authenticationManager.authenticate(authenticationToken);
		//PrincipalDetails객체는 로그인 요청을 한 유저의 모든 데이터가 들어가 있음

		PrincipalDetails principalDetailis = (PrincipalDetails) authentication.getPrincipal();
		System.out.println("Authentication : "+principalDetailis.getUser().getUsername());
		return authentication;
	}

	// 로그인 인증이 정상 실행됐을 때 => 
	// JWT Token 생	성해서 response에 담아주기
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		
		System.out.println("JwtAuthenticationFilter : successfulAuthentication() 호출");
		//인증된 사용자 객체 얻기  
		PrincipalDetails principalDetailis = (PrincipalDetails) authResult.getPrincipal();
		
		System.out.println(principalDetailis.getUser().getId());
		System.out.println(principalDetailis.getUser().getUsername());

		//인증키 생성 
		SecretKey key = Keys.hmacShaKeyFor(JwtProperties.getSecretKey());
		//인증 토큰 생성 : jwtToken
		String jwtAccessToken = Jwts.builder()
				.setClaims(Map.of("id", principalDetailis.getUser().getId()
								, "username", principalDetailis.getUser().getUsername()))
				//토큰의 사용 기간을 설정한다
				.setExpiration(new Date(System.currentTimeMillis()+JwtProperties.ACCESS_EXPIRATION_TIME))
				.signWith(key) //암호화용 키 : 원본데이터+암호화용 키로 => 암호화
				.compact();
		String jwtRefreshToken = JwtProperties.TOKEN_PREFIX+Jwts.builder()
				.setClaims(Map.of("id", principalDetailis.getUser().getId()
								, "username", principalDetailis.getUser().getUsername()))
				//토큰의 사용 기간을 설정한다
				.setExpiration(new Date(System.currentTimeMillis()+JwtProperties.REFRESH_EXPIRATION_TIME))
				.signWith(key) //암호화용 키 : 원본데이터+암호화용 키로 => 암호화
				.compact();
		
		System.out.println("첫토큰:"+jwtAccessToken);

		String e_JwtRefreshToken = URLEncoder.encode(jwtRefreshToken, "UTF-8");
        String cookieName = "refreshToken";
        Cookie cookie = new Cookie(cookieName, e_JwtRefreshToken);
        cookie.setHttpOnly(true);  //httponly 옵션 설정
        //cookie.setSecure(true); //https 옵션 설정
        cookie.setPath("/"); // 모든 곳에서 쿠키열람이 가능하도록 설정
        cookie.setMaxAge(60 * 60 * 24); //쿠키 만료시간 설정
        
        response.addCookie(cookie);
		//인증 토큰 출력 
		response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX+jwtAccessToken);
		
	}
	
}
