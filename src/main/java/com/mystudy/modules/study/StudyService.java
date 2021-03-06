package com.mystudy.modules.study;

import static com.mystudy.modules.study.form.StudyForm.VALID_PATH_PATTERN;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mystudy.modules.member.Member;
import com.mystudy.modules.study.form.StudyDescriptionForm;
import com.mystudy.modules.tag.Tag;
import com.mystudy.modules.zone.Zone;

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
		checkIfManager(member, study);
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
		Study study = studyRepository.findStudyWithTagsByPath(path);
		checkIfExistingStudy(path, study);
		checkIfManager(member, study);
		return study;
	}

	public Study getStudyToUpdateZone(Member member, String path) {
		Study study = studyRepository.findStudyWithZonesByPath(path);
		checkIfExistingStudy(path, study);
		checkIfManager(member, study);
		return study;
	}

	public Study getStudyToUpdateStatus(Member member, String path) {
		Study study = studyRepository.findStudyWithManagersByPath(path);
		checkIfExistingStudy(path, study);
		checkIfManager(member, study);
		return study;
	}

	private void checkIfManager(Member member, Study study) {
		if (!study.isManagedBy(member)) {
			throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
		}
	}

	private void checkIfExistingStudy(String path, Study study) {
		if (study == null) {
			throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
		}
	}

	public void publish(Study study) {
		study.publish();
	}

	public void close(Study study) {
		study.close();
	}

	public void startRecruit(Study study) {
		study.startRecruit();
	}

	public void stopRecruit(Study study) {
		study.stopRecruit();
	}

	public boolean isValidPath(String newPath) {
		if (!newPath.matches(VALID_PATH_PATTERN)) {
			return false;
		}

		return !studyRepository.existsByPath(newPath);
	}

	public void updateStudyPath(Study study, String newPath) {
		study.setPath(newPath);
	}

	public boolean isValidTitle(String newTitle) {
		return newTitle.length() <= 50;
	}

	public void updateStudyTitle(Study study, String newTitle) {
		study.setTitle(newTitle);
	}

	public void remove(Study study) {
		if (study.isRemovable()) {
			studyRepository.delete(study);
		} else {
			throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
		}
	}

	public void addMember(Study study, Member member) {
		study.addMember(member);
	}

	public void removeMember(Study study, Member member) {
		study.removeMember(member);
	}

	public Study getStudyToEnroll(String path) {
		Study study = studyRepository.findStudyOnlyByPath(path);
		checkIfExistingStudy(path, study);
		return study;
	}
}
