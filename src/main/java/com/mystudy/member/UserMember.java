package com.mystudy.member;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.mystudy.domain.Member;

import lombok.Getter;

@Getter
public class UserMember extends User{
	private  Member member;
	
	public UserMember(Member member) {
		super(member.getNickname(), member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
		this.member = member;
	}
}
