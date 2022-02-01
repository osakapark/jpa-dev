package com.mystudy.main;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.mystudy.domain.Member;
import com.mystudy.member.CurrentMember;

@Controller
public class MainController {
	@GetMapping("/")
	public String home(@CurrentMember Member member, Model model) {
		if(member != null ) {
			model.addAttribute(member);
		}
		return "index";
	}
	
	@GetMapping("/login")
	public String login() {
		return "login";
	}
}
