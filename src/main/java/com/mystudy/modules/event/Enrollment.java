package com.mystudy.modules.event;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.mystudy.modules.member.Member;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment {

	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private Event event;

	@ManyToOne
	private Member member;

	private LocalDateTime enrolledDateTime;

	private boolean accepted;

	private boolean attended;
}
