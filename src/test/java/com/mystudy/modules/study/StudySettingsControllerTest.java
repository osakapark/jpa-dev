package com.mystudy.modules.study;

import com.mystudy.modules.member.WithMember;
import com.mystudy.infra.MockMvcTest;
import com.mystudy.modules.member.Member;
import com.mystudy.modules.member.MemberFactory;
import com.mystudy.modules.member.MemberRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class StudySettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyFactory studyFactory;
    @Autowired MemberFactory memberFactory;
    @Autowired MemberRepository memberRepository;
    @Autowired StudyRepository studyRepository;
	
	@Test
	@WithMember("keesun")
	@DisplayName("스터디 소개 수정 폼 조회 - 실패 (권한 없는 유저)")
	void updateDescriptionForm_fail() throws Exception {
		Member whiteship = memberFactory.createMember("whiteship");
		Study study = studyFactory.createStudy("test-study", whiteship);

		mockMvc.perform(get("/study/" + study.getPath() + "/settings/description")).andExpect(status().isForbidden());
	}

	@Test
	@WithMember("keesun")
	@DisplayName("스터디 소개 수정 폼 조회 - 성공")
	void updateDescriptionForm_success() throws Exception {
		Member keesun = memberRepository.findByNickname("keesun");
		Study study = studyFactory.createStudy("test-study", keesun);

		mockMvc.perform(get("/study/" + study.getPath() + "/settings/description")).andExpect(status().isOk())
				.andExpect(view().name("study/settings/description"))
				.andExpect(model().attributeExists("studyDescriptionForm")).andExpect(model().attributeExists("member"))
				.andExpect(model().attributeExists("study"));
	}

	@Test
	@WithMember("keesun")
	@DisplayName("스터디 소개 수정 - 성공")
	void updateDescription_success() throws Exception {
		Member keesun = memberRepository.findByNickname("keesun");
		Study study = studyFactory.createStudy("test-study", keesun);

		String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
		mockMvc.perform(post(settingsDescriptionUrl).param("shortDescription", "short description")
				.param("fullDescription", "full description").with(csrf())).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl(settingsDescriptionUrl)).andExpect(flash().attributeExists("message"));
	}

	@Test
	@WithMember("keesun")
	@DisplayName("스터디 소개 수정 - 실패")
	void updateDescription_fail() throws Exception {
		Member keesun = memberRepository.findByNickname("keesun");
		Study study = studyFactory.createStudy("test-study", keesun);

		String settingsDescriptionUrl = "/study/" + study.getPath() + "/settings/description";
		mockMvc.perform(post(settingsDescriptionUrl).param("shortDescription", "")
				.param("fullDescription", "full description").with(csrf())).andExpect(status().isOk())
				.andExpect(model().hasErrors()).andExpect(model().attributeExists("studyDescriptionForm"))
				.andExpect(model().attributeExists("study")).andExpect(model().attributeExists("member"));
	}

}