package com.mystudy.member;

import java.util.List;

import javax.validation.Valid;

import org.apache.logging.log4j.message.SimpleMessage;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.domain.Member;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService{
	private final MemberRepository memberRepository;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Member processNewAccount(SignUpForm signUpForm) {
		Member newMember = saveNewMember(signUpForm);
		newMember.generateEmailCheckToken();
		sendSignUpConfirmEmail(newMember);
		return newMember;
	}

	private Member saveNewMember(@Valid SignUpForm signUpForm) {

		// @formatter:off
		Member member =  Member.builder()
				.email(signUpForm.getEmail())
				.nickname(signUpForm.getNickname())
				.password(passwordEncoder.encode(signUpForm.getPassword()))
				.createdByWeb(true)				
				.build();
		//@formatter:on
		return memberRepository.save(member);
	}

	public void sendSignUpConfirmEmail(Member newMember) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(newMember.getEmail());
		mailMessage.setSubject("회원가입 인증");
		mailMessage.setText(
				"/check-email-token?token=" + newMember.getEmailCheckToken() + "&email=" + newMember.getEmail());
		javaMailSender.send(mailMessage);
	}
	
	
	public void login(Member member) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
			new UserMember(member),
			member.getPassword(),
			List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(token);				
	}

	@Override
	public UserDetails loadUserByUsername(String emailOrNickname) throws UsernameNotFoundException {
		Member member = memberRepository.findByEmail(emailOrNickname);
		if (member == null) {
			member = memberRepository.findByNickname(emailOrNickname);
		}

		if (member == null) {
			throw new UsernameNotFoundException(emailOrNickname);
		}

		return new UserMember(member);
	}

}
