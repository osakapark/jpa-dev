package com.mystudy.modules.member;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@SuppressWarnings("serial")
@Getter
public class UserMember extends User{
	private  Member member;
	
	public UserMember(Member member) {
		super(member.getNickname(), member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
		this.member = member;
	}
}
