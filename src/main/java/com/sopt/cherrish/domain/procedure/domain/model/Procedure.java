package com.sopt.cherrish.domain.procedure.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "procedures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Procedure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 50)
	private String category;

	@Column(nullable = false)
	private Integer minDowntimeDays = 0;

	@Column(nullable = false)
	private Integer maxDowntimeDays = 0;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private Procedure(String name, String category, Integer minDowntimeDays, Integer maxDowntimeDays) {
		this.name = name;
		this.category = category;
		this.minDowntimeDays = minDowntimeDays != null ? minDowntimeDays : 0;
		this.maxDowntimeDays = maxDowntimeDays != null ? maxDowntimeDays : 0;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
