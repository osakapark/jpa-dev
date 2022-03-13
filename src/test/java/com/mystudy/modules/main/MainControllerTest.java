package com.mystudy.modules.main;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.mystudy.modules.member.MemberRepository;
import com.mystudy.modules.member.MemberService;
import com.mystudy.modules.member.form.SignUpForm;

@SpringBootTest
@AutoConfigureMockMvc
public class MainControllerTest {
	@Autowired MemberService memberService;
	@Autowired MemberRepository memberRepository;
	@Autowired MockMvc mockMvc;

	@BeforeEach
	void beforeEach() {
		SignUpForm signUpForm = new SignUpForm();
		signUpForm.setNickname("gae");
		signUpForm.setEmail("ssyang@gmail.com");
		signUpForm.setPassword("11111111");
		
		memberService.processNewMember(signUpForm);
	}
	
	@AfterEach
	void afterEach() {
		memberRepository.deleteAll();
	}
	
	@DisplayName("login success ")
	@Test
	void login_with_email() throws Exception {
		mockMvc.perform(post("/login")
			   .param("username", "ssyang@gmail.com")
			   .param("password", "11111111")
			   .with(csrf())
			   )
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("/"))
			   .andExpect(authenticated().withUsername("gae"));
		
	}
	
	@DisplayName("nickname login success ")
	@Test
	void login_with_nickname() throws Exception {
		mockMvc.perform(post("/login")
			   .param("username", "gae")
			   .param("password", "11111111")
			   .with(csrf())
			   )
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("/"))
			   .andExpect(authenticated().withUsername("gae"));
		
	}
	
	@DisplayName("login fail")
	@Test
	void login_fail() throws Exception {
		mockMvc.perform(post("/login")
			   .param("username", "gae")
			   .param("password", "111111111")
			   .with(csrf())
			   )
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("/login?error"))
			   .andExpect(unauthenticated());
		
	}
	
	@DisplayName("login out")
	@Test
	void logout() throws Exception {
		mockMvc.perform(post("/logout")			   
			   .with(csrf())
			   )
			   .andExpect(status().is3xxRedirection())
			   .andExpect(redirectedUrl("/"))
			   .andExpect(unauthenticated());
		
	}
	
}
