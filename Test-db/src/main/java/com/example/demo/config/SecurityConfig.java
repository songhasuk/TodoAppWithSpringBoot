package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.demo.config.jwt.JwtAuthenticationFilter;
import com.example.demo.config.jwt.JwtAuthorizationFilter;
import com.example.demo.repository.UserRepository;



// https://github.com/spring-projects/spring-security/issues/10822 참고
@Configuration
@EnableWebSecurity // 시큐리티 활성화 -> 기본 스프링 필터체인에 등록
public class SecurityConfig {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CorsConfig corsConfig;

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	//SecurityFilterChain : 기본적으로 주어지는  ==> 베이직 시큐리티 필터
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);
		//세션 정책 상수 설정
		/*
		 SessionCreationPolicy.ALWAYS      - 스프링시큐리티가 항상 세션을 생성
	     SessionCreationPolicy.IF_REQUIRED - 스프링시큐리티가 필요시 생성(기본) 
	     SessionCreationPolicy.NEVER       - 스프링시큐리티가 생성하지않지만, 기존에 존재하면 사용
	     SessionCreationPolicy.STATELESS   - 스프링시큐리티가 생성하지도않고 기존것을 사용하지도 않음,
	                                         JWT 같은 토큰방식을 쓸때 사용하는 설정 
	     */
		http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		http.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable);
		//.formLogin() .httpBasic() ==> 로그인폼 인증 작업과 베이직 토큰 사용을 않하겠다는 설정
		
		;// 추가적인 => 커스텀 필터 등록 => 기존 시큐리티 설정을 사용안하고
		http.apply(new MyCustomDsl());
		
		http.authorizeHttpRequests(authroize -> 
			authroize
			.requestMatchers("/api/v1/user/**") //인증 처리를 수행할 요청-주소(URL)를 설정
			.hasAnyAuthority("ROLE_USER", "ROLE_MANAGER", "ROLE_ADMIN") //여러개의 권한 중 하나라도 있으면 성공 
			.requestMatchers("/api/v1/manager/**")
			.hasAnyAuthority("ROLE_MANAGER","ROLE_ADMIN")
			.requestMatchers("/api/v1/admin/**")
			.hasAuthority("ROLE_ADMIN") //반드시 해당 권한만 허가  
			.anyRequest().permitAll() //비회원도 사용 할 수 있게 설정
		);
		
		return http.build();
		//기본적인 보안설정(SecurityFilterChain) 끝나고 추가적으로 커스텀 필터가 실행해 보안설정 적용됨
	}
	
	//커스텀 필터 클래스 정의 : AuthenticationManager객체는 따로 생성해줘야됨
	//(이유): [로그인form 기반 인증을 사용하지 않고] 인증처리를 할려면 => Manager객체를 따로 생성해 줘야된다.
	public class MyCustomDsl extends AbstractHttpConfigurer<MyCustomDsl, HttpSecurity> {
		@Override
		public void configure(HttpSecurity http) throws Exception {
		    //AuthenticationManager로 로그인한 사용자의 객체 등록
			AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
			http
					.addFilter(corsConfig.corsFilter())
					//로그인 후 [토큰을 발급]해주는 필터
					.addFilter(new JwtAuthenticationFilter(authenticationManager))
					//JwtAuthorizationFilter : 발급된 토큰을 전달 받아서 실행되는 URL 전에
					//토큰을 이용하여 인증 객체를 생성하여 설정함
					//토큰을 인증하여 유저 객체를 생성함
					.addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository));
		}
	}

}
