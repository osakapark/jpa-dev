package com.mystudy.settings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mystudy.WithMember;
import com.mystudy.domain.Member;
import com.mystudy.domain.Tag;
import com.mystudy.member.MemberRepository;
import com.mystudy.member.MemberService;
import com.mystudy.settings.form.TagForm;
import com.mystudy.tag.TagRepository;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class SettingsControllerTest {
	@Autowired
	MockMvc mockMvc;
	@Autowired
	MemberRepository memberRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	ObjectMapper objectMapper;
	@Autowired
	TagRepository tagRepository;
	@Autowired
	MemberService memberService;

	@AfterEach
	void afterEach() {
		memberRepository.deleteAll();
	}

	@WithMember("yaong")
	@DisplayName("계정의 tag 수정 form")
	@Test
	void updateTagsForm() throws Exception {
		mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
				.andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW_NAME))
				.andExpect(model().attributeExists("member")).andExpect(model().attributeExists("whitelist"))
				.andExpect(model().attributeExists("tags"));
	}

    @WithMember("yaong")
    @DisplayName("계정에 태그 추가")
    @Test
	void addTag() throws Exception {
		TagForm  tagForm = new TagForm();
		tagForm.setTagTitle("newTag");
		
		mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL +"/add")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tagForm))
				.with(csrf()))
				.andExpect(status().isOk());
		
		Tag newTag = tagRepository.findByTitle("newTag");
		assertNotNull(newTag);
		Member aaa= memberRepository.findByNickname("yaong");
		assertTrue(aaa.getTags().contains(newTag));		
	}
    
    @WithMember("yaong")
    @DisplayName("계정에 태그 삭제")
    @Test
	void removeTag() throws Exception {
    	Member member01 = memberRepository.findByNickname("yaong");    	
		Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
		memberService.addTag(member01,  newTag);
		
		assertTrue(member01.getTags().contains(newTag));
		
		
		TagForm tagForm = new TagForm();
		tagForm.setTagTitle("newTag");
				
		mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL +"/remove")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(tagForm))
				.with(csrf()))
				.andExpect(status().isOk());
		
		
		assertFalse(member01.getTags().contains(newTag));		
	}


	@WithMember("mal")
	@DisplayName("profile 수정 - 정상경우")
	@Test
	void updateProfileForm() throws Exception {
		String bio = "short bio..";
		mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL).param("bio", bio).with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
				.andExpect(flash().attributeExists("message"));
		Member memMal = memberRepository.findByNickname("mal");
		assertEquals(bio, memMal.getBio());
	}

	@WithMember("mal")
	@DisplayName("profile 수정 - 오류")
	@Test
	void updateProfile_error() throws Exception {
		String bio = "longn bio longn bio longn bio longn bio longn bio longn bio longn bio longn bio longn bio";
		mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL).param("bio", bio).with(csrf()))
				.andExpect(status().isOk()).andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW_NAME))
				.andExpect(model().attributeExists("member")).andExpect(model().attributeExists("profile"))
				.andExpect(model().hasErrors());
		Member memMal = memberRepository.findByNickname("mal");
		assertNull(memMal.getBio());
	}

	@WithMember("cat")
	@DisplayName("패스워드 수정폼")
	@Test
	void updatePassword_form() throws Exception {
		mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL)).andExpect(status().isOk())
				.andExpect(model().attributeExists("member")).andExpect(model().attributeExists("passwordForm"));
	}

	@WithMember("cat")
	@DisplayName("password 정상")
	@Test
	void updatePassword_success() throws Exception {
		mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL).param("newPassword", "12345678")
				.param("newPasswordConfirm", "12345678").with(csrf())).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
				.andExpect(flash().attributeExists("message"));

		Member memberCat = memberRepository.findByNickname("cat");
		assertTrue(passwordEncoder.matches("12345678", memberCat.getPassword()));
	}

	@WithMember("cat")
	@DisplayName("password 불일치")
	@Test
	void updatePassword_fail() throws Exception {
		mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL).param("newPassword", "12345678")
				.param("newPasswordConfirm", "12345671").with(csrf())).andExpect(status().isOk())
				.andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW_NAME)).andExpect(model().hasErrors())
				.andExpect(model().attributeExists("passwordForm")).andExpect(model().attributeExists("member"));

	}

}
