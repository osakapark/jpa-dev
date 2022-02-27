package com.mystudy.domain;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.mystudy.member.UserMember;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
	private List<Enrollment> enrollments;

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

    private boolean isAlreadyEnrolled(UserMember userAccount) {
    	Member member = userAccount.getMember();
        for (Enrollment e : this.enrollments) {
            if (e.getMember().equals(member)) {
                return true;
            }
        }
        return false;
    }
	
}
