package com.mystudy.event;

import java.time.LocalDateTime;
import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.mystudy.domain.Enrollment;
import com.mystudy.domain.Event;
import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import com.mystudy.event.form.EventForm;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

	private final EventRepository eventRepository;
	private final ModelMapper modelMapper;
	private final EnrollmentRepository enrollmentRepository;

	public Event createEvent(Event event, Study study, Member member) {
		event.setCreatedBy(member);
		event.setCreationDateTime(LocalDateTime.now());
		event.setStudy(study);
		return eventRepository.save(event);
	}

	public void updateEvent(Event event, EventForm eventForm) {
		modelMapper.map(eventForm, event);
		event.acceptWaitingList();
	}

	public void deleteEvent(Event event) {
		eventRepository.delete(event);
	}

	public void newEnrollment(Event event, Member member) {
		if (!enrollmentRepository.existsByEventAndMember(event, member)) {
			Enrollment enrollment = new Enrollment();
			enrollment.setEnrolledDateTime(LocalDateTime.now());
			enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
			enrollment.setMember(member);
			event.addEnrollment(enrollment);
			enrollmentRepository.save(enrollment);
		}
	}

	public void cancelEnrollment(Event event, Member member) {
		Enrollment enrollment = enrollmentRepository.findByEventAndMember(event, member);
		if (!enrollment.isAttended()) {
			event.removeEnrollment(enrollment);
			enrollmentRepository.delete(enrollment);
			event.acceptNextWaitingEnrollment();
		}
	}

	public void acceptEnrollment(Event event, Enrollment enrollment) {
		event.accept(enrollment);
	}

	public void rejectEnrollment(Event event, Enrollment enrollment) {
		event.reject(enrollment);
	}

	public void checkInEnrollment(Enrollment enrollment) {
		enrollment.setAttended(true);
	}

	public void cancelCheckInEnrollment(Enrollment enrollment) {
		enrollment.setAttended(false);
	}
}
