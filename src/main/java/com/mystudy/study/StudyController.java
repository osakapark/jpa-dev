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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	@GetMapping("/study/{path}")
	private String viewStudy(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudy(path);
		model.addAttribute(member);
		model.addAttribute(study);

		return "study/view";
	}

	@GetMapping("/study/{path}/members")
	public String viewStudyMembers(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudy(path);
		model.addAttribute(member);
		model.addAttribute(study);

		return "study/members";
	}

	@GetMapping("/study/{path}/join")
	public String joinStudy(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyRepository.findStudyWithMembersByPath(path);
		studyService.addMember(study, member);
		System.out.println("joinStudy:" + member.getNickname());
		return "redirect:/study/" + study.getEncodedPath() + "/members";
	}

	@PostMapping("/study/join")
	public String joinStudyPost(@CurrentMember Member member, @RequestParam String path) {
		Study study = studyRepository.findStudyWithMembersByPath(path);
		studyService.addMember(study, member);
		return "redirect:/study/" + study.getEncodedPath() + "/members";
	}

	@GetMapping("/study/{path}/leave")
	public String leaveStudy(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyRepository.findStudyWithMembersByPath(path);
		studyService.removeMember(study, member);
		System.out.println("leaveStudy:" + member.getNickname());
		return "redirect:/study/" + study.getEncodedPath() + "/members";
	}

	@PostMapping("/study/leave")
	public String leaveStudyPost(@CurrentMember Member member, @RequestParam String path) {
		Study study = studyRepository.findStudyWithMembersByPath(path);
		studyService.removeMember(study, member);
		return "redirect:/study/" + study.getEncodedPath() + "/members";
	}
}
