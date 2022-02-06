package com.mystudy.domain;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;

import com.mystudy.member.UserMember;

import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@formatter:off	
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
		@NamedAttributeNode("tags"), 
		@NamedAttributeNode("zones"),
		@NamedAttributeNode("managers"), 
		@NamedAttributeNode("members") }
)
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = {
		@NamedAttributeNode("tags"),		
		@NamedAttributeNode("managers")}
)
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = {
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withManagers", attributeNodes = {
        @NamedAttributeNode("managers")})
@NamedEntityGraph(name = "Study.withMembers", attributeNodes = {
        @NamedAttributeNode("members")})
//@formatter:on
@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Study {
	@Id
	@GeneratedValue
	private Long id;

	@Builder.Default
	@ManyToMany
	private Set<Member> managers = new HashSet<>();

	@Builder.Default
	@ManyToMany
	private Set<Member> members = new HashSet<>();

	@Column(unique = true)
	private String path;

	private String title;

	private String shortDescription;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String fullDescription;

	@Lob
	@Basic(fetch = FetchType.EAGER)
	private String image;

	@Builder.Default
	@ManyToMany
	private Set<Tag> tags = new HashSet<>();

	@Builder.Default
	@ManyToMany
	private Set<Zone> zones = new HashSet<>();

	private LocalDateTime publishedDateTime;

	private LocalDateTime closedDateTime;

	private LocalDateTime recruitingUpdatedDateTime;

	private boolean recruiting;

	private boolean published;

	private boolean closed;

	private boolean useBanner;

	public void addManager(Member member) {
		this.managers.add(member);
	}

	public boolean isJoinable(UserMember userMember) {
		Member member = userMember.getMember();
		return this.isPublished() && this.isRecruiting() && !this.members.contains(member)
				&& !this.managers.contains(member);

	}

	public boolean isMember(UserMember userMember) {
		return this.members.contains(userMember.getMember());
	}

	public boolean isManager(UserMember userMember) {
		return this.managers.contains(userMember.getMember());
	}

	public String getImage() {
		return image != null ? image : "/images/apeach_banner02.png";
	}

	public void publish() {
		if (!this.closed && !this.published) {
			this.published = true;
			this.publishedDateTime = LocalDateTime.now();
		} else {
			throw new RuntimeException("스터디를 공개할 수 없는 상태입니다. 스터디를 이미 공개했거나 종료했습니다.");
		}
	}

	public void close() {
		if (this.published && !this.closed) {
			this.closed = true;
			this.closedDateTime = LocalDateTime.now();
		} else {
			throw new RuntimeException("스터디를 종료할 수 없습니다. 스터디를 공개하지 않았거나 이미 종료한 스터디입니다.");
		}
	}

	public void startRecruit() {
		if (canUpdateRecruiting()) {
			this.recruiting = true;
			this.recruitingUpdatedDateTime = LocalDateTime.now();
		} else {
			throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
		}
	}

	public void stopRecruit() {
		if (canUpdateRecruiting()) {
			this.recruiting = false;
			this.recruitingUpdatedDateTime = LocalDateTime.now();
		} else {
			throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
		}
	}

	public boolean canUpdateRecruiting() {
		return this.published && this.recruitingUpdatedDateTime == null
				|| this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusSeconds(10));
	}

	public boolean isRemovable() {
		return !this.published; // TODO 모임을 했던 스터디는 삭제할 수 없다.
	}

	public void addMember(Member member) {
		this.getMembers().add(member);
	}

	public void removeMember(Member member) {
		this.getMembers().remove(member);
	}

	public String getEncodedPath() {
		return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
	}

}
