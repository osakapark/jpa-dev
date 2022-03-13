package com.mystudy.modules.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberRepository extends JpaRepository <Member, Long> {
	boolean existsByEmail(String email);
	
	boolean existsByNickname(String nickname);
	
	Member findByEmail(String email);
	
	Member findByNickname(String nickname);
}
