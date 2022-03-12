package com.mystudy.event;

import com.mystudy.WithMember;
import com.mystudy.domain.Member;
import com.mystudy.domain.Event;
import com.mystudy.domain.EventType;
import com.mystudy.domain.Study;
import com.mystudy.study.StudyControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest extends StudyControllerTest {

	@Autowired
	EventService eventService;
	@Autowired
	EnrollmentRepository enrollmentRepository;

	@Test
	@DisplayName("선착순 모임에 참가 신청 - 자동 수락")
	@WithMember("keesun")
	void newEnrollment_to_FCFS_event_accepted() throws Exception {
		Member whiteship = createMember("whiteship");
		Study study = createStudy("test-study", whiteship);
		Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

		mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

		Member keesun = memberRepository.findByNickname("keesun");
		isAccepted(keesun, event);
	}

	@Test
	@DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
	@WithMember("keesun")
	void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
		Member whiteship = createMember("whiteship");
		Study study = createStudy("test-study", whiteship);
		Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

		Member may = createMember("may");
		Member june = createMember("june");
		eventService.newEnrollment(event, may);
		eventService.newEnrollment(event, june);

		mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

		Member keesun = memberRepository.findByNickname("keesun");
		isNotAccepted(keesun, event);
	}

	@Test
	@DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
	@WithMember("keesun")
	void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
		Member keesun = memberRepository.findByNickname("keesun");
		Member whiteship = createMember("whiteship");
		Member may = createMember("may");
		Study study = createStudy("test-study", whiteship);
		Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

		eventService.newEnrollment(event, may);
		eventService.newEnrollment(event, keesun);
		eventService.newEnrollment(event, whiteship);

		isAccepted(may, event);
		isAccepted(keesun, event);
		isNotAccepted(whiteship, event);

		mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

		isAccepted(may, event);
		isAccepted(whiteship, event);
		assertNull(enrollmentRepository.findByEventAndMember(event, keesun));
	}

	@Test
	@DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
	@WithMember("keesun")
	void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
		Member keesun = memberRepository.findByNickname("keesun");
		Member whiteship = createMember("whiteship");
		Member may = createMember("may");
		Study study = createStudy("test-study", whiteship);
		Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

		eventService.newEnrollment(event, may);
		eventService.newEnrollment(event, whiteship);
		eventService.newEnrollment(event, keesun);

		isAccepted(may, event);
		isAccepted(whiteship, event);
		isNotAccepted(keesun, event);

		mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

		isAccepted(may, event);
		isAccepted(whiteship, event);
		assertNull(enrollmentRepository.findByEventAndMember(event, keesun));
	}

	private void isNotAccepted(Member whiteship, Event event) {
		assertFalse(enrollmentRepository.findByEventAndMember(event, whiteship).isAccepted());
	}

	private void isAccepted(Member account, Event event) {
		assertTrue(enrollmentRepository.findByEventAndMember(event, account).isAccepted());
	}

	@Test
	@DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
	@WithMember("keesun")
	void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
		Member whiteship = createMember("whiteship");
		Study study = createStudy("test-study", whiteship);
		Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, whiteship);

		mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

		Member keesun = memberRepository.findByNickname("keesun");
		isNotAccepted(keesun, event);
	}

	private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Member member) {
		Event event = new Event();
		event.setEventType(eventType);
		event.setLimitOfEnrollments(limit);
		event.setTitle(eventTitle);
		event.setCreationDateTime(LocalDateTime.now());
		event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
		event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
		event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
		return eventService.createEvent(event, study, member);
	}

}