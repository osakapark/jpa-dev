package com.mystudy.study;

import com.mystudy.member.CurrentMember;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import com.mystudy.domain.Tag;
import com.mystudy.domain.Zone;
import com.mystudy.study.form.StudyDescriptionForm;
import com.mystudy.tag.TagForm;
import com.mystudy.tag.TagRepository;
import com.mystudy.tag.TagService;
import com.mystudy.zone.ZoneForm;
import com.mystudy.zone.ZoneRepository;

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
		attributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
		return "redirect:/study/" + getPath(path) + "/settings/description";
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
		attributes.addFlashAttribute("message", "스터디 이미지를 수정했습니다.");
		return "redirect:/study/" + getPath(path) + "/settings/banner";
	}

	private String getPath(String path) {
		return URLEncoder.encode(path, StandardCharsets.UTF_8);
	}

	@PostMapping("/banner/enable")
	public String enableStudyBanner(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(member, path);
		studyService.enableStudyBanner(study);
		return "redirect:/study/" + getPath(path) + "/settings/banner";
	}

	@PostMapping("/banner/disable")
	public String disableStudyBanner(@CurrentMember Member member, @PathVariable String path) {
		Study study = studyService.getStudyToUpdate(member, path);
		studyService.disableStudyBanner(study);
		return "redirect:/study/" + getPath(path) + "/settings/banner";
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
    public String publishStudy(@CurrentMember Member member, @PathVariable String path,
                               RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(member, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message", "스터디를 공개했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentMember Member member, @PathVariable String path,
                             RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(member, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentMember Member member, @PathVariable String path, Model model,
                               RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(member, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentMember Member member, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(member, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

}
