package com.mystudy.modules.study;

import com.mystudy.modules.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyFactory {

	@Autowired
	StudyService studyService;
	@Autowired
	StudyRepository studyRepository;

	public Study createStudy(String path, Member manager) {
		Study study = new Study();
		study.setPath(path);
		studyService.createNewStudy(study, manager);
		return study;
	}

}
