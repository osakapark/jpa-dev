package com.mystudy.event;

import com.mystudy.domain.Member;
import com.mystudy.domain.Enrollment;
import com.mystudy.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndMember(Event event, Member member);

    Enrollment findByEventAndMember(Event event, Member member);
}
