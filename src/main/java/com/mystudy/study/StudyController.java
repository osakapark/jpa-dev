package com.mystudy.study;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import com.mystudy.member.CurrentMember;
import com.mystudy.study.form.StudyForm;
import com.mystudy.study.validator.StudyFormValidator;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StudyController {

	private final StudyRepository studyRepository;
	private final StudyService studyService;
	private final ModelMapper modelMapper;
	private final StudyFormValidator studyFormValidator;

	@InitBinder("studyForm")
	public void studyFormInitBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(studyFormValidator);
	}
	
	@GetMapping("/study/{path}")
	private String viewStudy(@CurrentMember Member member, @PathVariable String path, Model model) {
		model.addAttribute(member);
		model.addAttribute(studyRepository.findByPath(path));
		return "study/view";
	}

	@GetMapping("/new-study")
	public String newStudyForm(@CurrentMember Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(new StudyForm());
		return "study/form";
	}

	@PostMapping("/new-study")
	public String newStudySubmit(@CurrentMember Member member, @Valid StudyForm studyForm, Errors errors, Model model) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return "study/form";
		}
		Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), member);
		return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);

	}
}
