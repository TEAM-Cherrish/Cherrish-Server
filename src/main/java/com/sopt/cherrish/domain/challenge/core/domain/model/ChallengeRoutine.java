package com.sopt.cherrish.domain.challenge.core.domain.model;

import java.time.LocalDate;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
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
@Table(name = "challenge_routines", indexes = {
	@Index(name = "idx_challenge_scheduled", columnList = "challenge_id,scheduled_date"),
	@Index(name = "idx_challenge_complete", columnList = "challenge_id,is_complete")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeRoutine extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "challenge_id", nullable = false)
	private Challenge challenge;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, name = "scheduled_date")
	private LocalDate scheduledDate;

	@Column(nullable = false, name = "is_complete")
	private Boolean isComplete = false;

	@Builder
	private ChallengeRoutine(Challenge challenge, String name, LocalDate scheduledDate) {
		validateScheduledDateWithinChallengePeriod(challenge, scheduledDate);
		this.challenge = challenge;
		this.name = name;
		this.scheduledDate = scheduledDate;
	}

	private void validateScheduledDateWithinChallengePeriod(Challenge challenge, LocalDate scheduledDate) {
		if (challenge == null || scheduledDate == null) {
			// JPA가 기존 엔티티를 로드할 때 Builder가 호출되지 않으므로 이 조건에 도달하지 않음
			// 방어적 코드: null 입력 시 검증 스킵 (실제 생성 시에는 @Column(nullable = false)로 보장)
			throw new IllegalArgumentException("challenge와 scheduledDate는 null일 수 없습니다");
		}
		validateDateWithinChallengePeriod(challenge, scheduledDate);
	}

	public void complete() {
		this.isComplete = true;
	}

	/**
	 * 완료 상태 토글
	 */
	public void toggleCompletion() {
		this.isComplete = !this.isComplete;
	}

	/**
	 * 주어진 날짜가 챌린지 기간 내인지 검증 (루틴 수정 가능 여부 판단용)
	 * @param date 검증할 날짜 (일반적으로 현재 날짜)
	 * @throws ChallengeException 챌린지 기간 외의 날짜인 경우
	 */
	public void validateOperationDateWithinChallengePeriod(LocalDate date) {
		 validateDateWithinChallengePeriod(this.challenge, date);
	}

	/**
	 * 주어진 날짜가 챌린지 기간 내인지 검증 (공통 로직)
	 * @param challenge 챌린지
	 * @param date 검증할 날짜
	 * @throws ChallengeException 챌린지 기간 외의 날짜인 경우
	 */
	private void validateDateWithinChallengePeriod(Challenge challenge, LocalDate date) {
		LocalDate startDate = challenge.getStartDate();
		LocalDate endDate = challenge.getEndDate();

		if (date.isBefore(startDate) || date.isAfter(endDate)) {
			throw new ChallengeException(
				ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD
			);
		}
	}

	public boolean isScheduledFor(LocalDate date) {
		return this.scheduledDate.equals(date);
	}
}
