package com.mystudy;

import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.mystudy.member.MemberService;
import com.mystudy.member.form.SignUpForm;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WithMemberSecurityContextFactory implements WithSecurityContextFactory<WithMember>{

	private final MemberService memberService;

	@Override
	public SecurityContext createSecurityContext(WithMember withMember) {
		String nickname = withMember.value();
		
		SignUpForm signUpForm = new SignUpForm();
		signUpForm.setNickname(nickname);
		signUpForm.setEmail(nickname + "@gmail.com");
		signUpForm.setPassword("12345678");
		memberService.processNewMember(signUpForm);
		
		UserDetails principal = memberService.loadUserByUsername(nickname);
		Authentication authentication 
		  = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
		
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		return context;		    
	}
	
	
}
