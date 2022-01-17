package com.mystudy.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.domain.Member;

@Transactional
public interface MemberRepository extends JpaRepository <Member, Long> {
	boolean existsByEmail(String email);
	
	boolean existsByNickname(String nickname);
	
	Member findByEmail(String email);
	
	Member findByNickname(String nickname);
}
