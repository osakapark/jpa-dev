package com.mystudy.modules.event;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mystudy.modules.member.Member;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndMember(Event event, Member member);

    Enrollment findByEventAndMember(Event event, Member member);
}
