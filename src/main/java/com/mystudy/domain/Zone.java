package com.mystudy.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"city","province"}))
public class Zone {
	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String localNameOfCity;

	@Column(nullable = true)
	private String province;

	@Override
	public String toString() {
		return String.format("%s(%s)/%s", city, localNameOfCity, province);
	}
}
