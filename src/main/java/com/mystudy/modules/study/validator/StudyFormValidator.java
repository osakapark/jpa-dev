package com.mystudy.modules.study.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.mystudy.modules.study.StudyRepository;
import com.mystudy.modules.study.form.StudyForm;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

	private final StudyRepository studyRepository;

	@Override
	public boolean supports(Class<?> clazz) {
		return StudyForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		StudyForm studyForm = (StudyForm) target;
		if (studyRepository.existsByPath(studyForm.getPath())) {
			errors.rejectValue("path", "wrong.path", "해당 스터디 경로 이미 쓴다");
		}

	}

}
