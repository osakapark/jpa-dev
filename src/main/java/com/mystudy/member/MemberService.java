package com.mystudy.member;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import org.modelmapper.ModelMapper;
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
import com.mystudy.domain.Tag;
import com.mystudy.member.form.SignUpForm;
import com.mystudy.settings.form.Notifications;
import com.mystudy.settings.form.Profile;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
	private final MemberRepository memberRepository;
	private final JavaMailSender javaMailSender;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;

	public Member processNewMember(SignUpForm signUpForm) {
		Member newMember = saveNewMember(signUpForm);
		sendSignUpConfirmEmail(newMember);
		return newMember;
	}

	private Member saveNewMember(@Valid SignUpForm signUpForm) {
		signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
		Member member = modelMapper.map(signUpForm, Member.class);
		member.generateEmailCheckToken();

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
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(new UserMember(member),
				member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(token);
	}

	@Transactional(readOnly = true)
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

	public void completeSignUp(Member member) {
		member.completeSignUp();
		login(member);
	}

	public void updateProfile(Member member, Profile profile) {
		modelMapper.map(profile, member);
		memberRepository.save(member);
	}

	public void updatePassword(Member member, String newPassword) {
		member.setPassword(passwordEncoder.encode(newPassword));
		memberRepository.save(member);
	}

	public void updateNotifications(Member member, Notifications notifications) {
		modelMapper.map(notifications, member);
		memberRepository.save(member);
	}

	public void updateNickname(Member member, String nickname) {
		member.setNickname(nickname);
		memberRepository.save(member);
		login(member);
	}

	public void sendLoginLink(Member member) {
		member.generateEmailCheckToken();
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(member.getEmail());
		mailMessage.setSubject("스터디갈래, 로그인 링크");
		mailMessage.setText("/login-by-email?token=" + member.getEmailCheckToken() + "&email=" + member.getEmail());
		javaMailSender.send(mailMessage);
	}

	public void addTag(Member member, Tag tag) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		byId.ifPresent(a -> a.getTags().add(tag));
	}

	public Set<Tag> getTags(Member member) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		return byId.orElseThrow().getTags();
	}

	public void removeTag(Member member, Tag tag) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		byId.orElseThrow().getTags().remove(tag);
	}
}
