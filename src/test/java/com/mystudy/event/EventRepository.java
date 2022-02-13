package com.mystudy.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.mystudy.domain.Event;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

}