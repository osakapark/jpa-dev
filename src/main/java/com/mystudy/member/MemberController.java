package com.mystudy.member;

import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.yaml.snakeyaml.tokens.WhitespaceToken;

import com.mystudy.domain.Member;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final SignUpFormValidator signUpFormValidator;
	private final MemberService memberService;
	private final MemberRepository memberRepository;

	@InitBinder("signUpForm")
	public void initBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(signUpFormValidator);
	}

	@GetMapping("/sign-up")
	public String signUpForm(Model model) {
		model.addAttribute(new SignUpForm());
		return "member/sign-up";
	}

	@PostMapping("/sign-up")
	public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
		if (errors.hasErrors()) {
			return "member/sign-up";
		}

		Member member = memberService.processNewAccount(signUpForm);
		memberService.login(member);
		return "redirect:/";
	}

	@GetMapping("/check-email-token")
	public String checkEmailToken(String token, String email, Model model) {
		Member member = memberRepository.findByEmail(email);
		String view = "member/checked-email";
		if(member == null ) {
			model.addAttribute("error", "wrong.email");
			return view;
		}
		
		if(!member.isValidToken(token)) {
			model.addAttribute("error", "wrong.token");
			return view;
		}		
		
		member.completeSignUp();
		memberService.login(member);
		model.addAttribute("numberOfUser", memberRepository.count());
		model.addAttribute("nickname", member.getNickname());
		return view;
	}
	
	@GetMapping("/check-email")
	public String checkEmail(@CurrentUser Member member, Model model) {
		model.addAttribute("email", member.getEmail());
		return "member/check-email";
	}
	
	@GetMapping("/resend-confirm-email")
	public String resendConfirmEmail(@CurrentUser Member member, Model model) {
		if (!member.canSendConfirmEmail()) {
			model.addAttribute("error", "이메일 인증은 1분에 1번만");
			model.addAttribute("email", member.getEmail());
			return "member/check-email";
		}
		
		member.generateEmailCheckToken();	//2021.12.31 선생님 확인 
		memberService.sendSignUpConfirmEmail(member);
		return "redirect:/";
	}

}
