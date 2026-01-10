package com.sopt.cherrish.domain.challenge.core.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;
import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(mappedBy = "challenge", fetch = FetchType.LAZY)
	private ChallengeStatistics statistics;

	@Column(nullable = false, name = "user_id")
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "homecare_routine")
	private HomecareRoutine homecareRoutine;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, name = "is_active")
	private Boolean isActive = true;

	@Column(nullable = false, name = "total_days")
	private Integer totalDays = 7;

	@Column(nullable = false, name = "start_date")
	private LocalDate startDate;

	@Column(nullable = false, name = "end_date")
	private LocalDate endDate;

	@Builder
	private Challenge(Long userId, HomecareRoutine homecareRoutine,
		String title, LocalDate startDate) {
		this.userId = userId;
		this.homecareRoutine = homecareRoutine;
		this.title = title;
		this.startDate = startDate;
		this.endDate = startDate.plusDays(this.totalDays - 1); // 7일 챌린지
	}

	/**
	 * 챌린지 루틴 생성 팩토리 메서드
	 * @param routineNames 루틴명 리스트
	 * @return 생성된 챌린지 루틴 리스트 (루틴명 개수 × 7일)
	 */
	public List<ChallengeRoutine> createChallengeRoutines(List<String> routineNames) {
		List<ChallengeRoutine> routines = new ArrayList<>();

		for (int day = 0; day < totalDays; day++) {
			LocalDate scheduledDate = startDate.plusDays(day);

			for (String routineName : routineNames) {
				routines.add(ChallengeRoutine.builder()
					.challenge(this)
					.name(routineName)
					.scheduledDate(scheduledDate)
					.build());
			}
		}

		return routines;
	}

	public void complete() {
		this.isActive = false;
	}

	/**
	 * 소유자 검증
	 * @param userId 검증할 사용자 ID
	 * @throws ChallengeException 소유자가 아닌 경우
	 */
	public void validateOwner(Long userId) {
		if (!this.userId.equals(userId)) {
			throw new ChallengeException(
				ChallengeErrorCode.UNAUTHORIZED_ACCESS
			);
		}
	}

	/**
	 * 현재 챌린지 진행 일차 계산 (1-indexed)
	 * @param today 현재 날짜
	 * @return 현재 일차 (1부터 시작)
	 */
	public int getCurrentDay(LocalDate today) {
		// 챌린지 시작 전: 0 반환
		if (today.isBefore(startDate)) {
			return 0;
		}

		// 챌린지 종료 후: 최대 일차 반환
		if (today.isAfter(endDate)) {
			return totalDays;
		}

		return (int) ChronoUnit.DAYS.between(startDate, today) + 1;
	}

	/**
	 * 오늘부터 챌린지 종료일까지 커스텀 루틴 생성
	 * @param routineName 루틴명
	 * @param today 현재 날짜
	 * @return 생성된 챌린지 루틴 리스트
	 */
	public List<ChallengeRoutine> createCustomRoutinesFromToday(String routineName, LocalDate today) {
		validateDateWithinChallengePeriod(today);

		List<ChallengeRoutine> routines = new ArrayList<>();
		LocalDate currentDate = today;

		while (!currentDate.isAfter(endDate)) {
			routines.add(ChallengeRoutine.builder()
				.challenge(this)
				.name(routineName)
				.scheduledDate(currentDate)
				.build());
			currentDate = currentDate.plusDays(1);
		}

		return routines;
	}

	/**
	 * 날짜가 챌린지 기간 내인지 검증
	 * @param date 검증할 날짜
	 * @throws ChallengeException 챌린지 기간 외의 날짜인 경우
	 */
	private void validateDateWithinChallengePeriod(LocalDate date) {
		if (date.isBefore(startDate) || date.isAfter(endDate)) {
			throw new ChallengeException(
				ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD
			);
		}
	}
}
