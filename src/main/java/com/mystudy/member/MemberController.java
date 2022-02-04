package com.mystudy.member;

import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mystudy.domain.Member;
import com.mystudy.member.form.SignUpForm;
import com.mystudy.member.validator.SignUpFormValidator;

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

		Member member = memberService.processNewMember(signUpForm);
		memberService.login(member);
		return "redirect:/";
	}

	@GetMapping("/check-email-token")
	public String checkEmailToken(String token, String email, Model model) {
		Member member = memberRepository.findByEmail(email);
		String view = "member/checked-email";
		if (member == null) {
			model.addAttribute("error", "wrong.email");
			return view;
		}

		if (!member.isValidToken(token)) {
			model.addAttribute("error", "wrong.token");
			return view;
		}

		memberService.completeSignUp(member);
		model.addAttribute("numberOfUser", memberRepository.count());
		model.addAttribute("nickname", member.getNickname());
		return view;
	}

	@GetMapping("/check-email")
	public String checkEmail(@CurrentMember Member member, Model model) {
		model.addAttribute("email", member.getEmail());
		return "member/check-email";
	}

	@GetMapping("/resend-confirm-email")
	public String resendConfirmEmail(@CurrentMember Member member, Model model) {
		if (!member.canSendConfirmEmail()) {
			model.addAttribute("error", "이메일 인증은 1분에 1번만");
			model.addAttribute("email", member.getEmail());
			return "member/check-email";
		}

		member.generateEmailCheckToken(); // 2021.12.31 선생님 확인
		memberService.sendSignUpConfirmEmail(member);
		return "redirect:/";
	}

	@GetMapping("/profile/{nickname}")
	public String viewProfile(@PathVariable String nickname, Model model, @CurrentMember Member member) {
		
		Member memberToView = memberService.getMember(nickname);
		model.addAttribute(memberToView);
		model.addAttribute("isOwner", memberToView.equals(member));
		

		return "member/profile";
	}

	@GetMapping("/email-login")
	public String emailLoginForm() {
		return "member/email-login";
	}

	@PostMapping("/email-login")
	public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes) {
		Member member = memberRepository.findByEmail(email);
		if (member == null) {
			model.addAttribute("error", "유효한 이메일이 아니여");
			return "member/email-login";
		}

		if (!member.canSendConfirmEmail()) {
			model.addAttribute("error", "이메일 인증 요청은 10 초에 한번씩");
			return "member/email-login";
		}

		memberService.sendLoginLink(member);
		attributes.addFlashAttribute("message", "인증메일 보냈다.");
		return "redirect:/email-login";
	}

	@GetMapping("/login-by-email")
	public String loginByEmail(String token, String email, Model model) {
		Member member = memberRepository.findByEmail(email);
		String view = "member/logged-in-by-email";
		if (member == null || !member.isValidToken(token)) {
			return view;
		}

		memberService.login(member);
		return view;

	}
}
