package com.mystudy.modules.member.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mystudy.modules.member.MemberRepository;
import com.mystudy.modules.member.form.SignUpForm;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

	private final MemberRepository memberRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(SignUpForm.class);
	}

	@Override
	public void validate(Object target, Errors errors) {
		// SignUpForm signUpForm = signUpForm.
		SignUpForm signUpForm = (SignUpForm) target;
		if (memberRepository.existsByEmail(signUpForm.getEmail())) {
			errors.rejectValue("email", "invalid.email", new Object[] { signUpForm.getEmail() }, "이미사용중 메일이다");
		}

		if (memberRepository.existsByNickname(signUpForm.getNickname())) {
			errors.rejectValue("nickname", "invalid.nickname", new Object[] { signUpForm.getNickname() }, "이미사용중 닉네임 ");
		}
	}
}
