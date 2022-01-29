package com.mystudy.settings;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mystudy.domain.Member;
import com.mystudy.member.CurrentUser;
import com.mystudy.member.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SettingsController {

	static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
	static final String SETTINGS_PROFILE_URL = "/settings/profile";

	private final MemberService memberService;

	@GetMapping(SETTINGS_PROFILE_URL)
	public String profileUpdateForm(@CurrentUser Member member, Model model) {
		model.addAttribute(member);
		model.addAttribute(new Profile(member));
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
}
