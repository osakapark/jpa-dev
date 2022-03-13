package com.mystudy.modules.member.form;

import org.hibernate.validator.constraints.Length;

import lombok.Data;

@Data
public class PasswordForm {

	@Length(min = 8, max = 50)
	private String newPassword;

	@Length(min = 8, max = 50)
	private String newPasswordConfirm;
}
