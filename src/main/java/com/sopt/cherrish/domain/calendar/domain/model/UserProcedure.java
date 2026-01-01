package com.sopt.cherrish.domain.calendar.domain.model;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_procedures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProcedure {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// TODO: User 엔티티 구현 후 연결
	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "user_id", nullable = false)
	// private User user;
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@Column(nullable = false)
	private LocalDateTime scheduledAt;

	@Column
	private Integer downtimeDays;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	private UserProcedure(Long userId, Procedure procedure, LocalDateTime scheduledAt, Integer downtimeDays) {
		this.userId = userId;
		this.procedure = procedure;
		this.scheduledAt = scheduledAt;
		this.downtimeDays = downtimeDays;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
