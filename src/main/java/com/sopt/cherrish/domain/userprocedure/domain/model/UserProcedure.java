package com.sopt.cherrish.domain.userprocedure.domain.model;

import java.time.LocalDateTime;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

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

@Entity
@Table(name = "user_procedures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProcedure extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@Column(nullable = false, name = "scheduled_at")
	private LocalDateTime scheduledAt;

	@Column(name = "downtime_days")
	private Integer downtimeDays;

	@Builder
	private UserProcedure(Long userId, Procedure procedure, LocalDateTime scheduledAt, Integer downtimeDays) {
		this.userId = userId;
		this.procedure = procedure;
		this.scheduledAt = scheduledAt;
		this.downtimeDays = downtimeDays;
	}
}
