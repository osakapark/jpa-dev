package com.mystudy.modules.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberFactory {
	@Autowired
	MemberRepository memberRepository;

	public Member createMember(String nickname) {
		Member whiteship = new Member();
		whiteship.setNickname(nickname);
		whiteship.setEmail(nickname + "@email.com");
		memberRepository.save(whiteship);
		return whiteship;
	}
}
