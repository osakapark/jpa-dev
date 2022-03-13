package com.mystudy.modules.member;

import static com.mystudy.modules.member.SettingsController.ROOT;
import static com.mystudy.modules.member.SettingsController.SETTINGS;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystudy.modules.member.form.NicknameForm;
import com.mystudy.modules.member.form.Notifications;
import com.mystudy.modules.member.form.PasswordForm;
import com.mystudy.modules.member.form.Profile;
import com.mystudy.modules.member.validator.NicknameValidator;
import com.mystudy.modules.member.validator.PasswordFormValidator;
import com.mystudy.modules.tag.Tag;
import com.mystudy.modules.tag.TagForm;
import com.mystudy.modules.tag.TagRepository;
import com.mystudy.modules.tag.TagService;
import com.mystudy.modules.zone.Zone;
import com.mystudy.modules.zone.ZoneForm;
import com.mystudy.modules.zone.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingsController {

	static final String ROOT = "/";
	static final String SETTINGS = "settings";
	static final String PROFILE = "/profile";
	static final String PASSWORD = "/password";
	static final String NOTIFICATIONS = "/notifications";
	static final String MEMBER = "/member";
	static final String TAGS = "/tags";
	static final String ZONES = "/zones";

	private final MemberService memberService;
	private final ModelMapper modelMapper;
	private final NicknameValidator nicknameValidator;
	private final TagService tagService;
	private final TagRepository tagrepository;
	private final ZoneRepository zoneRepository;
	private final ObjectMapper objectMapper;

	@InitBinder("passwordForm")
	public void passwordForminitBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(new PasswordFormValidator());
	}

	@InitBinder("nicknameForm")
	public void nicknameForminitBinder(WebDataBinder webDataBinder) {
		webDataBinder.addValidators(nicknameValidator);
	}

	@GetMapping(PROFILE)
	public String updateProfileForm(@CurrentMember Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, Profile.class));
		return SETTINGS + PROFILE;
	}

	@PostMapping(PROFILE)
	public String updateProfile(@CurrentMember Member member, @Valid Profile profile, Errors errors, Model model,
			RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS + PROFILE;
		}

		memberService.updateProfile(member, profile);
		attributes.addFlashAttribute("message", "프로필 수정했다");
		return "redirect:/" + SETTINGS + PROFILE;
	}

	@GetMapping(PASSWORD)
	public String updatePasswordForm(@CurrentMember Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(new PasswordForm());
		return SETTINGS + PASSWORD;
	}

	@PostMapping(PASSWORD)
	public String updatePassword(@CurrentMember Member member, @Valid PasswordForm passwordForm, Errors errors,
			Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS + PASSWORD;
		}

		memberService.updatePassword(member, passwordForm.getNewPassword());
		attributes.addFlashAttribute("message", "패스워드 변경했다.");
		return "redirect:/" + SETTINGS + PASSWORD;
	}

	@GetMapping(NOTIFICATIONS)
	public String updateNotificationsForm(@CurrentMember Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, Notifications.class));
		return SETTINGS + NOTIFICATIONS;
	}

	@PostMapping(NOTIFICATIONS)
	public String updateNotifications(@CurrentMember Member member, @Valid Notifications notifications, Errors errors,
			Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS + NOTIFICATIONS;
		}

		memberService.updateNotifications(member, notifications);
		attributes.addFlashAttribute("message", "알림설정 변경했다.");
		return "redirect:/" + SETTINGS + NOTIFICATIONS;
	}

	@GetMapping(TAGS)
	public String updateTags(@CurrentMember Member member, Model model) throws JsonProcessingException {
		model.addAttribute(member);
		Set<Tag> tags = memberService.getTags(member);
		model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));
		System.out.println("tags :" + tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

		List<String> allTags = tagrepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));
		System.out.println("whitelist :" + objectMapper.writeValueAsString(allTags));
		return SETTINGS + TAGS;
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(TAGS + "/add")
	@ResponseBody
	public ResponseEntity addTag(@CurrentMember Member member, @RequestBody TagForm tagForm) {
		Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
		memberService.addTag(member, tag);
		return ResponseEntity.ok().build();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(TAGS + "/remove")
	@ResponseBody
	public ResponseEntity removeTag(@CurrentMember Member member, @RequestBody TagForm tagForm) {
		String title = tagForm.getTagTitle();
		Tag tag = tagrepository.findByTitle(title);
		if (tag == null) {
			return ResponseEntity.badRequest().build();
		}
		memberService.removeTag(member, tag);
		return ResponseEntity.ok().build();
	}

	@GetMapping(ZONES)
	public String updateZoneForm(@CurrentMember Member member, Model model) throws JsonProcessingException {
		model.addAttribute(member);

		Set<Zone> zones = memberService.getZones(member);
		model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));
		System.out.println("zones1 :" + zones.stream().map(Zone::toString).collect(Collectors.toList()));

		List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
		System.out.println("zones2 :" + objectMapper.writeValueAsString(allZones));

		return SETTINGS + ZONES;
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(ZONES + "/add")
	@ResponseBody
	public ResponseEntity addZone(@CurrentMember Member member, @RequestBody ZoneForm zoneForm) {
		Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
		if (zone == null) {
			return ResponseEntity.badRequest().build();
		}

		memberService.addZone(member, zone);
		return ResponseEntity.ok().build();
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(ZONES + "/remove")
	@ResponseBody
	public ResponseEntity removeZone(@CurrentMember Member member, @RequestBody ZoneForm zoneForm) {
		Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
		if (zone == null) {
			return ResponseEntity.badRequest().build();
		}

		memberService.removeZone(member, zone);
		return ResponseEntity.ok().build();
	}

	@GetMapping(MEMBER)
	public String updateMemberForm(@CurrentMember Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(modelMapper.map(member, NicknameForm.class));
		return SETTINGS + MEMBER;
	}

	@PostMapping(MEMBER)
	public String updateMember(@CurrentMember Member member, @Valid NicknameForm nicknameForm, Errors errors,
			Model model, RedirectAttributes attributes) {
		if (errors.hasErrors()) {
			model.addAttribute(member);
			return SETTINGS + MEMBER;
		}

		memberService.updateNickname(member, nicknameForm.getNickname());
		attributes.addFlashAttribute("message", "닉네임 변경했다.");
		return "redirect:/" + SETTINGS + MEMBER;
	}
}
