package com.mystudy.modules.study;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.mystudy.modules.member.WithMember;
import com.mystudy.infra.MockMvcTest;
import com.mystudy.modules.member.Member;
import com.mystudy.modules.member.MemberFactory;
import com.mystudy.modules.member.MemberRepository;
import com.mystudy.modules.study.Study;
import com.mystudy.modules.study.StudyRepository;
import com.mystudy.modules.study.StudyService;

import lombok.RequiredArgsConstructor;

@MockMvcTest
public class StudyControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberFactory memberFactory;
    @Autowired StudyFactory studyFactory;

	@AfterEach
	void afterEach() {
		memberRepository.deleteAll();
	}

	@Test
	@WithMember("keesun")
	@DisplayName("????????? ?????? ??? ??????")
	void createStudyForm() throws Exception {
		mockMvc.perform(get("/new-study")).andExpect(status().isOk()).andExpect(view().name("study/form"))
				.andExpect(model().attributeExists("member")).andExpect(model().attributeExists("studyForm"));
	}

	@Test
	@WithMember("keesun")
	@DisplayName("????????? ?????? - ??????")
	void createStudy_success() throws Exception {
		mockMvc.perform(post("/new-study").param("path", "test-path").param("title", "study title")
				.param("shortDescription", "short description of a study")
				.param("fullDescription", "full description of a study").with(csrf()))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/study/test-path"));

		Study study = studyRepository.findByPath("test-path");
		assertNotNull(study);
		Member account = memberRepository.findByNickname("keesun");
		assertTrue(study.getManagers().contains(account));
	}

	@Test
	@WithMember("keesun")
	@DisplayName("????????? ?????? - ??????")
	void createStudy_fail() throws Exception {
		mockMvc.perform(post("/new-study").param("path", "wrong path").param("title", "study title")
				.param("shortDescription", "short description of a study")
				.param("fullDescription", "full description of a study").with(csrf())).andExpect(status().isOk())
				.andExpect(view().name("study/form")).andExpect(model().hasErrors())
				.andExpect(model().attributeExists("studyForm")).andExpect(model().attributeExists("member"));

		Study study = studyRepository.findByPath("test-path");
		assertNull(study);
	}

	@Test
	@WithMember("keesun")
	@DisplayName("????????? ??????")
	void viewStudy() throws Exception {
		Study study = new Study();
		study.setPath("test-path");
		study.setTitle("test study");
		study.setShortDescription("short description");
		study.setFullDescription("<p>full description</p>");

		Member keesun = memberRepository.findByNickname("keesun");
		studyService.createNewStudy(study, keesun);

		mockMvc.perform(get("/study/test-path")).andExpect(view().name("study/view"))
				.andExpect(model().attributeExists("member")).andExpect(model().attributeExists("study"));
	}

	@Test
	@WithMember("keesun")
	@DisplayName("study ??????")
	void joinStudy() throws Exception {
		Member whiteship = memberFactory.createMember("a1");
		Study study = studyFactory.createStudy("test-study", whiteship);

		mockMvc.perform(get("/study/" + study.getPath() + "/join")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

		Member keesun = memberRepository.findByNickname("keesun");
		assertTrue(study.getMembers().contains(keesun));

	}

	@Test
	@WithMember("keesun")
	@DisplayName("????????? ??????")
	void leaveStudy() throws Exception {
		Member whiteship = memberFactory.createMember("whiteship");
		Study study = studyFactory.createStudy("test-study", whiteship);

		Member keesun = memberRepository.findByNickname("keesun");
		studyService.addMember(study, keesun);

		mockMvc.perform(get("/study/" + study.getPath() + "/leave")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/members"));

		assertFalse(study.getMembers().contains(keesun));
	}



}
