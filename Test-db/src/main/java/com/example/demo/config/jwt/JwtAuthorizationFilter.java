package com.example.demo.config.jwt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.example.demo.config.auto.PrincipalDetails;
import com.example.demo.dto.UsersDTO;
import com.example.demo.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


// 인가 필터 : 인증-정보(JWT 토큰)를 확인하는 코드  
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("토큰 인증");



        String header = request.getHeader(JwtProperties.HEADER_STRING);

        System.out.println(JwtProperties.HEADER_STRING);
        System.out.println("header : " + header);

        if (header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
       

        String token = header.replace(JwtProperties.TOKEN_PREFIX, "");
        System.out.println("dfsfdsf"+token);

        SecretKey key = Keys.hmacShaKeyFor(JwtProperties.getSecretKey());

        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = claimsJws.getBody();
            String username = String.valueOf(claims.get("username"));
            System.out.println("이름:" + username);
            System.out.println(jwtAccessTokenIsExpired(token));

            if (jwtAccessTokenIsExpired(token)) {
                handleExpiredAccessToken(request, response);
            } else {
                processValidAccessToken(username);
            }

        } catch (ExpiredJwtException ex) {
            handleExpiredAccessToken(request, response);
        } catch (Exception e) {
            // Token validation failed, continue without setting authentication
            System.out.println("Token validation failed: " + e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private boolean jwtAccessTokenIsExpired(String token) {
    	SecretKey key = Keys.hmacShaKeyFor(JwtProperties.getSecretKey());
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
    
    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    //리플레쉬 토큰 유효성 검사
    private boolean jwtRefreshTokenIsValid(String refreshToken) {
    	System.out.println("접근");
        try {
            SecretKey key = Keys.hmacShaKeyFor(JwtProperties.getSecretKey());
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception e) {
        	System.out.println("리프레위 오류:"+e.getMessage());
            return false;
        }
    }

    //엑세스 코드 신규발급
    private String generateNewAccessTokenFromRefreshToken(String refreshToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(JwtProperties.getSecretKey());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(refreshToken)
                    .getBody();

            String newAccessToken = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(new Date(System.currentTimeMillis() + JwtProperties.ACCESS_EXPIRATION_TIME))
                    .signWith(key)
                    .compact();

            return newAccessToken;
        } catch (Exception e) {
            return null;
        }
    }
    
    //리플레쉬 토큰 발급 유무 확인
    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        String refreshToken = URLDecoder.decode(extractRefreshToken(request), "UTF-8").replace("Bearer ", "");
        System.out.println("엑세스 토큰" + refreshToken);

        if (refreshToken != null && jwtRefreshTokenIsValid(refreshToken)) {
            
            String newAccessToken = generateNewAccessTokenFromRefreshToken(refreshToken);
            System.out.println("★뉴토큰★"+newAccessToken);
            response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + newAccessToken);
            return ;
        }
    }
    //엑세스 코드 유효기간 문제 없으면 실행되는 코드
    private void processValidAccessToken(String username) {
        if (username != null) {
            UsersDTO user = userRepository.findByUsername(username);

            PrincipalDetails principalDetails = new PrincipalDetails(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principalDetails,
                    null,
                    principalDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }
}
