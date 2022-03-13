package com.mystudy.modules.member;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.ManyToAny;

import com.mystudy.modules.study.Study;
import com.mystudy.modules.tag.Tag;
import com.mystudy.modules.zone.Zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue
	private Long id;

	@Column(unique = true)
	private String email;

	@Column(unique = true)
	private String nickname;

	private String password;
	private boolean emailVerified;
	private String emailCheckToken;

	private LocalDateTime joinDttm;
	private LocalDateTime emailCheckTokenGeneratedDttm;
	private String bio;

	private String url;
	private String occupation;
	private String location;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String profileImage;

	private boolean studyCreatedByEmail;

	@Builder.Default()
	private boolean studyCreatedByWeb = true;

	private boolean studyEnrollmentResultByEmail;

	@Builder.Default()
	private boolean studyEnrollmentResultByWeb = true;
	private boolean studyUpdatedByEmail;

	@Builder.Default()
	private boolean studyUpdatedByWeb = true;

	@Builder.Default()
	@ManyToMany
	private Set<Tag> tags = new HashSet<>();

	@Builder.Default()
	@ManyToMany
	private Set<Zone> zones = new HashSet<>();

	public void generateEmailCheckToken() {
		this.emailCheckToken = UUID.randomUUID().toString();
		this.emailCheckTokenGeneratedDttm = LocalDateTime.now();
	}

	public void completeSignUp() {
		this.emailVerified = true;
		this.joinDttm = LocalDateTime.now();
	}

	public boolean isValidToken(String token) {
		return this.emailCheckToken.equals(token);
	}

	public boolean canSendConfirmEmail() {
		return this.emailCheckTokenGeneratedDttm.isBefore(LocalDateTime.now().minusSeconds(10));
	}


}