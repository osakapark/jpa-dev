package com.mystudy.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

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

	private boolean createdByEmail;

	private boolean createdByWeb;

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
		return this.emailCheckTokenGeneratedDttm.isBefore(LocalDateTime.now().minusMinutes(1));
	}
}