package com.sopt.cherrish.domain.userprocedure.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.userprocedure.domain.vo.DowntimePeriod;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "user_procedures",
	indexes = {
		@Index(name = "idx_user_procedures_user_id_scheduled_at", columnList = "user_id, scheduled_at")
	}
)
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

	@Column(nullable = false, name = "scheduled_at")
	private LocalDateTime scheduledAt;

	@Column(name = "downtime_days", nullable = false)
	private Integer downtimeDays;

	@Column(name = "recovery_target_date")
	private LocalDate recoveryTargetDate;

	@Builder
	private UserProcedure(
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		Integer downtimeDays,
		LocalDate recoveryTargetDate
	) {
		this.user = user;
		this.procedure = procedure;
		this.scheduledAt = scheduledAt;
		this.downtimeDays = downtimeDays;
		this.recoveryTargetDate = recoveryTargetDate;
	}

	/**
	 * 다운타임 기간 계산
	 * 시술 시작일로부터 민감기/주의기/회복기를 계산합니다.
	 *
	 * @return 다운타임 기간 (민감기, 주의기, 회복기 날짜 목록)
	 */
	public DowntimePeriod calculateDowntimePeriod() {
		return DowntimePeriod.calculate(this.downtimeDays, this.scheduledAt.toLocalDate());
	}

	/**
	 * 현재 날짜 기준 시술 후 회복 단계 계산
	 *
	 * @param today 현재 날짜
	 * @return 현재 시술 단계 (SENSITIVE, CAUTION, RECOVERY, COMPLETED)
	 */
	public ProcedurePhase calculateCurrentPhase(LocalDate today) {
		DowntimePeriod period = calculateDowntimePeriod();
		return ProcedurePhase.calculate(period, today);
	}
}
