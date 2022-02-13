package com.mystudy.event;

import java.time.LocalDateTime;
import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.mystudy.domain.Event;
import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

	private final EventRepository eventRepository;

	public Event createEvent(Event event, Study study, Member member) {
		event.setCreatedBy(member);
		event.setCreationDateTime(LocalDateTime.now());
		event.setStudy(study);
		return eventRepository.save(event);

	}
}
