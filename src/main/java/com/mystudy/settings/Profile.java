package com.mystudy.settings;

import org.hibernate.validator.constraints.Length;

import com.mystudy.domain.Member;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
public class Profile {
	@Length(max = 35)
	private String bio;
	
	@Length(max = 50)
	private String url;
	
	@Length(max = 50)
	private String occupation;
	
	@Length(max = 50)
	private String location;
	
	public Profile(Member member) {
		this.bio = member.getBio();
		this.url = member.getUrl();
		this.occupation = member.getOccupation();
		this.location = member.getLocation();
	}
}
