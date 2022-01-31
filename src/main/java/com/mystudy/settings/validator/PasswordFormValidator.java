package com.mystudy.settings.validator;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mystudy.settings.form.PasswordForm;

public class PasswordFormValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return PasswordForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		PasswordForm passwordForm = (PasswordForm) target;
		if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
			errors.rejectValue("newPassword", "wrong.value", "입력 패스워드 불일치");
		}
	}

}
