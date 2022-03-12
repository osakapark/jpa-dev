package com.mystudy.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;

import com.mystudy.member.UserMember;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@NamedEntityGraph(name = "Event.withEnrollments", attributeNodes = @NamedAttributeNode("enrollments"))

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Event {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Study study;

	@ManyToOne
	private Member createdBy;

	@Column(nullable = false)
	private String title;

	@Lob
	private String description;

	@Column(nullable = false)
	private LocalDateTime creationDateTime;

	@Column(nullable = false)
	private LocalDateTime endEnrollmentDateTime;

	@Column(nullable = false)
	private LocalDateTime startDateTime;

	@Column(nullable = false)
	private LocalDateTime endDateTime;

	@Column
	private Integer limitOfEnrollments;

	@OneToMany(mappedBy = "event")
	private List<Enrollment> enrollments = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private EventType eventType;

	public boolean isEnrollableFor(UserMember userAccount) {
		return isNotClosed() && !isAlreadyEnrolled(userAccount);
	}

	public boolean isDisenrollableFor(UserMember userAccount) {
		return isNotClosed() && isAlreadyEnrolled(userAccount);
	}

	private boolean isNotClosed() {
		return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
	}

	public boolean isAttended(UserMember userAccount) {
		Member account = userAccount.getMember();
		for (Enrollment e : this.enrollments) {
			if (e.getMember().equals(account) && e.isAttended()) {
				return true;
			}
		}

		return false;
	}

	public int numberOfRemainSpots() {
		return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
	}

	private boolean isAlreadyEnrolled(UserMember userAccount) {
		Member member = userAccount.getMember();
		for (Enrollment e : this.enrollments) {
			if (e.getMember().equals(member)) {
				return true;
			}
		}
		return false;
	}

	public long getNumberOfAcceptedEnrollments() {
		return this.enrollments.stream().filter(Enrollment::isAccepted).count();
	}

	public void addEnrollment(Enrollment enrollment) {
		this.enrollments.add(enrollment);
		enrollment.setEvent(this);
	}

	public void removeEnrollment(Enrollment enrollment) {
		this.enrollments.remove(enrollment);
		enrollment.setEvent(null);
	}

	public boolean isAbleToAcceptWaitingEnrollment() {
		return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
	}

	public boolean canAccept(Enrollment enrollment) {
		return this.eventType == EventType.CONFIRMATIVE && this.enrollments.contains(enrollment)
				&& !enrollment.isAttended() && !enrollment.isAccepted();
	}

	public boolean canReject(Enrollment enrollment) {
		return this.eventType == EventType.CONFIRMATIVE && this.enrollments.contains(enrollment)
				&& !enrollment.isAttended() && enrollment.isAccepted();
	}

	private List<Enrollment> getWaitingList() {
		return this.enrollments.stream().filter(enrollment -> !enrollment.isAccepted()).collect(Collectors.toList());
	}

	public void acceptWaitingList() {
		if (this.isAbleToAcceptWaitingEnrollment()) {
			var waitingList = getWaitingList();
			int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(),
					waitingList.size());
			waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
		}
	}

	public void acceptNextWaitingEnrollment() {
		if (this.isAbleToAcceptWaitingEnrollment()) {
			Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
			if (enrollmentToAccept != null) {
				enrollmentToAccept.setAccepted(true);
			}
		}
	}

	private Enrollment getTheFirstWaitingEnrollment() {
		for (Enrollment e : this.enrollments) {
			if (!e.isAccepted()) {
				return e;
			}
		}

		return null;
	}

}
