package com.mystudy.study;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.domain.Member;
import com.mystudy.domain.Study;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

	private final StudyRepository studyRepository;

	public Study createNewStudy(Study study, Member member) {
		Study newStudy = studyRepository.save(study);
		newStudy.addManager(member);
		return newStudy;
	}
}
