package com.mystudy.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystudy.modules.member.CurrentMember;
import com.mystudy.modules.member.Member;
import com.mystudy.modules.study.form.StudyDescriptionForm;
import com.mystudy.modules.tag.Tag;
import com.mystudy.modules.tag.TagForm;
import com.mystudy.modules.tag.TagRepository;
import com.mystudy.modules.tag.TagService;
import com.mystudy.modules.zone.Zone;
import com.mystudy.modules.zone.ZoneForm;
import com.mystudy.modules.zone.ZoneRepository;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {
	private final StudyRepository studyRepository;
	private final StudyService studyService;
	private final ModelMapper modelMapper;
	private final TagService tagService;
	private final TagRepository tagRepository;
	private final ZoneRepository zoneRepository;
	private final ObjectMapper objectMapper;

	@GetMapping("/description")
	public String viewStudySetting(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(member);
		model.addAttribute(study);
		model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
		return "study/settings/description";
	}

	@PostMapping("/description")
	public String updateStudyInfo(@CurrentMember Member member, @PathVariable String path,
			@Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model,
			RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdate(member, path);

		if (errors.hasErrors()) {
			model.addAttribute(member);
			model.addAttribute(study);
			return "study/settings/description";
		}

		studyService.updateStudyDescription(study, studyDescriptionForm);
		attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/description";
	}

	@GetMapping("/banner")
	public String studyImageForm(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(member);
		model.addAttribute(study);
		return "study/settings/banner";
	}

	@PostMapping("/banner")
	public String studyImageSubmit(@CurrentMember Member member, @PathVariable String path, String image,
			RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdate(member, path);
		studyService.updateStudyImage(study, image);
		attributes.addFlashAttribute("message", "????????? ???????????? ??????????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
	}

	@PostMapping("/banner/enable")
	public String enableStudyBanner(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(member, path);
		studyService.enableStudyBanner(study);
		return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
	}

	@PostMapping("/banner/disable")
	public String disableStudyBanner(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(member, path);
		studyService.disableStudyBanner(study);
		return "redirect:/study/" + study.getEncodedPath() + "/settings/banner";
	}

	@GetMapping("/tags")
	public String studyTagsForm(@CurrentMember Member member, @PathVariable String path, Model model)
			throws JsonProcessingException {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(member);
		model.addAttribute(study);

		model.addAttribute("tags", study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));
		List<String> allTagTitles = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
		return "study/settings/tags";
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/tags/add")
	@ResponseBody
	public ResponseEntity addTag(@CurrentMember Member member, @PathVariable String path,
			@RequestBody TagForm tagForm) {
		Study study = studyService.getStudyToUpdateTag(member, path);
		Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
		studyService.addTag(study, tag);
		return ResponseEntity.ok().build();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/tags/remove")
	@ResponseBody
	public ResponseEntity removeTag(@CurrentMember Member member, @PathVariable String path,
			@RequestBody TagForm tagForm) {
		Study study = studyService.getStudyToUpdateTag(member, path);
		Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
		if (tag == null) {
			return ResponseEntity.badRequest().build();
		}

		studyService.removeTag(study, tag);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/zones")
	public String studyZonesForm(@CurrentMember Member member, @PathVariable String path, Model model)
			throws JsonProcessingException {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(member);
		model.addAttribute(study);
		model.addAttribute("zones", study.getZones().stream().map(Zone::toString).collect(Collectors.toList()));
		List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
		return "study/settings/zones";
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/zones/add")
	@ResponseBody
	public ResponseEntity addZone(@CurrentMember Member member, @PathVariable String path,
			@RequestBody ZoneForm zoneForm) {
		Study study = studyService.getStudyToUpdateZone(member, path);
		Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
		if (zone == null) {
			return ResponseEntity.badRequest().build();
		}

		studyService.addZone(study, zone);
		return ResponseEntity.ok().build();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping("/zones/remove")
	@ResponseBody
	public ResponseEntity removeZone(@CurrentMember Member member, @PathVariable String path,
			@RequestBody ZoneForm zoneForm) {
		Study study = studyService.getStudyToUpdateZone(member, path);
		Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
		if (zone == null) {
			return ResponseEntity.badRequest().build();
		}

		studyService.removeZone(study, zone);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/study")
	public String studySettingForm(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdate(member, path);
		model.addAttribute(member);
		model.addAttribute(study);
		return "study/settings/study";
	}

	@PostMapping("/study/publish")
	public String publishStudy(@CurrentMember Member member, @PathVariable String path, RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		studyService.publish(study);
		attributes.addFlashAttribute("message", "???????????? ??????????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/study/close")
	public String closeStudy(@CurrentMember Member member, @PathVariable String path, RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		studyService.close(study);
		attributes.addFlashAttribute("message", "???????????? ??????????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/recruit/start")
	public String startRecruit(@CurrentMember Member member, @PathVariable String path, Model model,
			RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		if (!study.canUpdateRecruiting()) {
			attributes.addFlashAttribute("message", "1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
			return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
		}

		studyService.startRecruit(study);
		attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/recruit/stop")
	public String stopRecruit(@CurrentMember Member member, @PathVariable String path, Model model,
			RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdate(member, path);
		if (!study.canUpdateRecruiting()) {
			attributes.addFlashAttribute("message", "1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
			return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
		}

		studyService.stopRecruit(study);
		attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/study/path")
	public String updateStudyPath(@CurrentMember Member member, @PathVariable String path, String newPath, Model model,
			RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		if (!studyService.isValidPath(newPath)) {
			model.addAttribute(member);
			model.addAttribute(study);
			model.addAttribute("studyPathError", "??????????????? ????????? ?????? ????????? ????????? ??????");
			return "study/settings/study";
		}

		studyService.updateStudyPath(study, newPath);
		attributes.addFlashAttribute("message", "????????? ?????? ????????????");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/study/title")
	public String updateStudyTitle(@CurrentMember Member member, @PathVariable String path, String newTitle,
			Model model, RedirectAttributes attributes) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		if (!studyService.isValidTitle(newTitle)) {
			model.addAttribute(member);
			model.addAttribute(study);
			model.addAttribute("studyTitleError", "??????????????? ????????? ?????? ????????? ????????? ??????");
			return "study/settings/study";
		}

		studyService.updateStudyTitle(study, newTitle);
		attributes.addFlashAttribute("message", "????????? ?????? ????????????");
		return "redirect:/study/" + study.getEncodedPath() + "/settings/study";
	}

	@PostMapping("/study/remove")
	public String removeStudy(@CurrentMember Member member, @PathVariable String path, Model model) {
		Study study = studyService.getStudyToUpdateStatus(member, path);
		studyService.remove(study);
		return "redirect:/";
	}

}
