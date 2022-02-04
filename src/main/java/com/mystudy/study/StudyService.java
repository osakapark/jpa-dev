package com.mystudy.study;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.domain.Member;
import com.mystudy.domain.Study;
import com.mystudy.domain.Tag;
import com.mystudy.domain.Zone;
import com.mystudy.study.form.StudyDescriptionForm;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

	private final StudyRepository studyRepository;
	private final ModelMapper modelMapper;

	public Study createNewStudy(Study study, Member member) {
		Study newStudy = studyRepository.save(study);
		newStudy.addManager(member);
		return newStudy;
	}

	public Study getStudyToUpdate(Member member, String path) {
		Study study = this.getStudy(path);
		if (!member.isManagerOf(study)) {
			throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
		}

		return study;
	}

	public Study getStudy(String path) {
		Study study = this.studyRepository.findByPath(path);
		if (study == null) {
			throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
		}

		return study;
	}

	public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
		modelMapper.map(studyDescriptionForm, study);
	}

	public void updateStudyImage(Study study, String image) {
		study.setImage(image);
	}

	public void enableStudyBanner(Study study) {
		study.setUseBanner(true);
	}

	public void disableStudyBanner(Study study) {
		study.setUseBanner(false);
	}

	public void addTag(Study study, Tag tag) {
		study.getTags().add(tag);
	}

	public void removeTag(Study study, Tag tag) {
		study.getTags().remove(tag);
	}

	public void addZone(Study study, Zone zone) {
		study.getZones().add(zone);
	}

	public void removeZone(Study study, Zone zone) {
		study.getZones().remove(zone);
	}

	public Study getStudyToUpdateTag(Member member, String path) {
		Study study = studyRepository.findMemberWithTagsByPath(path);
		checkIfExistingStudy(path, study);
		checkIfManager(member, study);
		return study;
	}

	public Study getStudyToUpdateZone(Member member, String path) {
		Study study = studyRepository.findMemberWithZonesByPath(path);
		checkIfExistingStudy(path, study);
		checkIfManager(member, study);
		return study;
	}

	private void checkIfManager(Member member, Study study) {
		if (!member.isManagerOf(study)) {
			throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
		}
	}

	private void checkIfExistingStudy(String path, Study study) {
		if (study == null) {
			throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
		}
	}

}
