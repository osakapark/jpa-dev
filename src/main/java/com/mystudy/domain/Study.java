package com.mystudy.domain;

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

	public void addMember(Member member) {
		this.members.add(member);
	}

	public String getImage() {
		return image != null ? image : "/images/apeach_banner02.png";
	}
}
