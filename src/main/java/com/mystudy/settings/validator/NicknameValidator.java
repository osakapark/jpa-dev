package com.mystudy.settings.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import com.mystudy.domain.Member;
import com.mystudy.member.MemberRepository;
import com.mystudy.settings.form.NicknameForm;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NicknameValidator implements Validator {

	private final MemberRepository memberRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return NicknameForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		NicknameForm nicknameForm = (NicknameForm) target;
		Member byNickname = memberRepository.findByNickname(nicknameForm.getNickname());
		if (byNickname != null) {
			errors.rejectValue("newPassword", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
		}
	}
}