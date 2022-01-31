package com.mystudy.settings;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystudy.domain.Member;
import com.mystudy.domain.Tag;
import com.mystudy.member.CurrentUser;
import com.mystudy.member.MemberService;
import com.mystudy.settings.form.NicknameForm;
import com.mystudy.settings.form.Notifications;
import com.mystudy.settings.form.PasswordForm;
import com.mystudy.settings.form.Profile;
import com.mystudy.settings.form.TagForm;
import com.mystudy.settings.validator.NicknameValidator;
import com.mystudy.settings.validator.PasswordFormValidator;
import com.mystudy.tag.TagRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SettingsController {

	static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
	static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME;
	static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
	static final String SETTINGS_PASSWORD_URL = "/" + SETTINGS_PASSWORD_VIEW_NAME;
	static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";
	static final String SETTINGS_NOTIFICATIONS_URL = "/" + SETTINGS_NOTIFICATIONS_VIEW_NAME;
	static final String SETTINGS_MEMBER_VIEW_NAME = "settings/member";
	static final String SETTINGS_MEMBER_URL = "/" + SETTINGS_MEMBER_VIEW_NAME;
	static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
	static final String SETTINGS_TAGS_URL = "/" + SETTINGS_TAGS_VIEW_NAME;

	private final MemberService memberService;
	private final ModelMapper modelMapper;
	private final NicknameValidator nicknameValidator;
	private final TagRepository tagrepository;
	private final ObjectMapper objectMapper;

	@InitBinder("passwordForm")
	public void passwordForminitBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(new PasswordFormValidator());
	}

	@InitBinder("nicknameForm")
	public void nicknameForminitBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(nicknameValidator);
	}

	@GetMapping(SETTINGS_PROFILE_URL)
	public String updateProfileForm(@CurrentUser Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, Profile.class));
		return SETTINGS_PROFILE_VIEW_NAME;
	}

	@PostMapping(SETTINGS_PROFILE_URL)
	public String updateProfile(@CurrentUser Member member, @Valid Profile profile, Errors errors, Model model,
			RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS_PROFILE_VIEW_NAME;
		}

		memberService.updateProfile(member, profile);
		attributes.addFlashAttribute("message", "프로필 수정했다");
		return "redirect:" + SETTINGS_PROFILE_URL;
	}

	@GetMapping(SETTINGS_PASSWORD_URL)
	public String updatePasswordForm(@CurrentUser Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(new PasswordForm());
		return SETTINGS_PASSWORD_VIEW_NAME;
	}

	@PostMapping(SETTINGS_PASSWORD_URL)
	public String updatePassword(@CurrentUser Member member, @Valid PasswordForm passwordForm, Errors errors,
			Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS_PASSWORD_VIEW_NAME;
		}

		memberService.updatePassword(member, passwordForm.getNewPassword());
		attributes.addFlashAttribute("message", "패스워드 변경했다.");
		return "redirect:" + SETTINGS_PASSWORD_URL;
	}

	@GetMapping(SETTINGS_NOTIFICATIONS_URL)
	public String updateNotificationsForm(@CurrentUser Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, Notifications.class));
		return SETTINGS_NOTIFICATIONS_VIEW_NAME;
	}

	@PostMapping(SETTINGS_NOTIFICATIONS_URL)
	public String updateNotifications(@CurrentUser Member member, @Valid Notifications notifications, Errors errors,
			Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS_NOTIFICATIONS_VIEW_NAME;
		}

		memberService.updateNotifications(member, notifications);
		attributes.addFlashAttribute("message", "알림설정 변경했다.");
		return "redirect:" + SETTINGS_NOTIFICATIONS_URL;
	}

	@GetMapping(SETTINGS_TAGS_URL)
	public String updateTags(@CurrentUser Member member, Model model) throws JsonProcessingException {
		model.addAttribute(member);
		Set<Tag> tags = memberService.getTags(member);
		model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
		System.out.println("tags :" + tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
		
		List<String> allTags = tagrepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
		System.out.println("whitelist :" + objectMapper.writeValueAsString(allTags));
		return SETTINGS_TAGS_VIEW_NAME;
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(SETTINGS_TAGS_URL + "/add")
	@ResponseBody
	public ResponseEntity addTag(@CurrentUser Member member, @RequestBody TagForm tagForm) {
		String title = tagForm.getTagTitle();
		Tag tag = tagrepository.findByTitle(title);
		if (tag == null) {
			tag = tagrepository.save(Tag.builder().title(title).build());
		}
		memberService.addTag(member, tag);
		return ResponseEntity.ok().build();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(SETTINGS_TAGS_URL + "/remove")
	@ResponseBody
	public ResponseEntity removeTag(@CurrentUser Member member, @RequestBody TagForm tagForm) {
		String title = tagForm.getTagTitle();
		Tag tag = tagrepository.findByTitle(title);
		if (tag == null) {
			return ResponseEntity.badRequest().build();
		}
		memberService.removeTag(member, tag);
		return ResponseEntity.ok().build();
	}

	@GetMapping(SETTINGS_MEMBER_URL)
	public String updateMemberForm(@CurrentUser Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, NicknameForm.class));
		return SETTINGS_MEMBER_VIEW_NAME;
	}

	@PostMapping(SETTINGS_MEMBER_URL)
	public String updateMember(@CurrentUser Member member, @Valid NicknameForm nicknameForm, Errors errors, Model model,
			RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS_MEMBER_VIEW_NAME;
		}

		memberService.updateNickname(member, nicknameForm.getNickname());
		attributes.addFlashAttribute("message", "닉네임 변경했다.");
		return "redirect:" + SETTINGS_MEMBER_URL;
	}
}
