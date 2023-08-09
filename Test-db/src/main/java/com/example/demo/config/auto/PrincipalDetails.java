package com.example.demo.config.auto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.demo.dto.UsersDTO;


//UserDetails 내장 인터페이스
public class PrincipalDetails implements UserDetails {

    private static final long serialVersionUID = -951226953749557253L;
	private UsersDTO user;

    public PrincipalDetails(UsersDTO user) {
        this.user = user;
    }

    public UsersDTO getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //유저 권한을 부여하는 메소드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        user.getRoleList().forEach(r -> {
            authorities.add(() -> {
                return r;
            });
        });
        return authorities;
    }
}
