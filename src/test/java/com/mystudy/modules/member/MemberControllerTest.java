package com.mystudy.modules.member;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.infra.MockMvcTest;
import com.mystudy.infra.mail.EmailMessage;
import com.mystudy.infra.mail.EmailService;
import com.mystudy.modules.member.Member;
import com.mystudy.modules.member.MemberRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

@MockMvcTest
public class MemberControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private MemberRepository memberRepository;
	
	@MockBean
	private EmailService emailService;

	@DisplayName("인증메일 학인 - 입력값 오류")
	@Test
	void checkEmailToken_with_wrong_input() throws Exception {
		mockMvc.perform(get("/check-email-token")
				.param("token","ggggg")
				.param("email", "derereer.com"))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("error"))
				.andExpect(view().name("member/checked-email"))
				.andExpect(unauthenticated());
	}
	
	
	@DisplayName("인증메일 학인 - 입력값 정상")
	@Test
	void checkEmailToken() throws Exception {
		 Member member = Member.builder()
				 .email("test@gmail.com")
				 .password("12345678")
				 .nickname("gaegae")
				 .build();
		Member newMember = memberRepository.save(member);
		newMember.generateEmailCheckToken();
		
		mockMvc.perform(get("/check-email-token")				
				.param("token",newMember.getEmailCheckToken())
				.param("email",newMember.getEmail()))
				.andExpect(status().isOk())
				.andExpect(model().attributeDoesNotExist("error"))
				.andExpect(model().attributeExists("nickname"))
				.andExpect(model().attributeExists("numberOfUser"))
				.andExpect(view().name("member/checked-email"))
				.andExpect(authenticated().withUsername("gaegae"));
	}
	
	
	@DisplayName("회원가입화면 보이는 테스트")
	@Test
	void signUpForm() throws Exception {
		mockMvc.perform(get("/sign-up"))
		.andExpect(status().isOk())
		.andExpect(view().name("member/sign-up"))
		.andExpect(model().attributeExists("signUpForm"))
		.andExpect(unauthenticated())		;
	}
	

	@DisplayName("입력오류")
	@Test
	void signUpSubmit_with_wrong_input() throws Exception {
		mockMvc.perform(post("/sign-up")
		.param("nickname", "gae")
		.param("email", "ssyang")
		.param("password","1122")
		.with(csrf()))
		.andExpect(status().isOk())
		.andExpect(view().name("member/sign-up"))
		.andExpect(unauthenticated());
	}
	

	@DisplayName("입력정상dd")
	@Test
	void signUpSubmit_with_correct_input() throws Exception {
		mockMvc.perform(post("/sign-up")
		.param("nickname", "gae2121212")
		.param("email", "ssyang@gae.com")
		.param("password","11221111")
		.with(csrf()))
		.andExpect(status().is3xxRedirection())
		.andExpect(view().name("redirect:/"))
		.andExpect(authenticated().withUsername("gae2121212"));
		
		Member member  =memberRepository.findByEmail("ssyang@gae.com");
		assertNotNull(member);
		assertNotEquals(member.getPassword(), "11221111");		
		System.out.println("password := " + member.getPassword());
		
		then(emailService).should().sendEmail(any(EmailMessage.class));
		System.out.println("emailService:" + emailService.getClass());
				
		
	}	

}
