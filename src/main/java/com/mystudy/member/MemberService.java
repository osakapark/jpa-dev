package com.mystudy.member;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.mystudy.config.AppProperties;
import com.mystudy.domain.Member;
import com.mystudy.domain.Tag;
import com.mystudy.domain.Zone;
import com.mystudy.mail.EmailMessage;
import com.mystudy.mail.EmailService;
import com.mystudy.member.form.SignUpForm;
import com.mystudy.settings.form.Notifications;
import com.mystudy.settings.form.Profile;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
	private final MemberRepository memberRepository;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;
	private final ModelMapper modelMapper;
	private final AppProperties appProperties;
	private final TemplateEngine templateEngine;

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
		Context context = new Context();
		context.setVariable("link",
				"/check-email-token?token=" + newMember.getEmailCheckToken() + "&email=" + newMember.getEmail());
		context.setVariable("nickname", newMember.getNickname());
		context.setVariable("linkName", "이메일 인증하자");
		context.setVariable("message", "스터디 갈래 서비스를 사용하려면 링크 클릭하자");
		context.setVariable("host", appProperties.getHost());
		String message = templateEngine.process("mail/simple-link", context);

		EmailMessage emailMessage = EmailMessage.builder().to(newMember.getEmail()).subject("스터다 갈래 회원 가입 인증")
				.message(message).build();
		emailService.sendEmail(emailMessage);
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
		Context context = new Context();
		context.setVariable("link",
				"/login-by-email?token=" + member.getEmailCheckToken() + "&email=" + member.getEmail());
		context.setVariable("nickname", member.getNickname());
		context.setVariable("linkName", "스터디 갈래, 로그인 ");
		context.setVariable("message", "로그이니 하려면 아래 링크 클릭하자");
		context.setVariable("host", appProperties.getHost());
		String message = templateEngine.process("mail/simple-link", context);
	
		EmailMessage emailMessage = EmailMessage.builder()
				.to(member.getEmail())
				.subject("스터디 갈래, 로그인 링크")
				.message(message)
				.build();
		emailService.sendEmail(emailMessage);
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

	public Set<Zone> getZones(Member member) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		return byId.orElseThrow().getZones();
	}

	public void addZone(Member member, Zone zone) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		byId.ifPresent(a -> a.getZones().add(zone));
	}

	public void removeZone(Member member, Zone zone) {
		Optional<Member> byId = memberRepository.findById(member.getId());
		byId.ifPresent(a -> a.getZones().remove(zone));
	}
}
