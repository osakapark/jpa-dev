package com.mystudy.event;

import java.time.LocalDateTime;
import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
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

	public Event createEvent(Event event, Study study, Member member) {
		event.setCreatedBy(member);
		event.setCreationDateTime(LocalDateTime.now());
		event.setStudy(study);
		return eventRepository.save(event);
	}

	public void updateEvent(Event event, EventForm eventForm) {
		modelMapper.map(eventForm, event);
	}

	public void deleteEvent(Event event) {
		eventRepository.delete(event);
	}
}
