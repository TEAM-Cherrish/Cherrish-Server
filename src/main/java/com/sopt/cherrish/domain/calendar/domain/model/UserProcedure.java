package com.sopt.cherrish.domain.calendar.domain.model;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "user_procedures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProcedure extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@Column(nullable = false)
	private LocalDateTime scheduledAt;

	@Column
	private Integer downtimeDays;

	@Builder
	private UserProcedure(User user, Procedure procedure, LocalDateTime scheduledAt, Integer downtimeDays) {
		this.user = user;
		this.procedure = procedure;
		this.scheduledAt = scheduledAt;
		this.downtimeDays = downtimeDays;
	}
}
