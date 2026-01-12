package com.sopt.cherrish.domain.challenge.demo.domain.model;

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
@Table(name = "demo_challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoChallenge extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(mappedBy = "demoChallenge", fetch = FetchType.LAZY)
	private DemoChallengeStatistics statistics;

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

	@Column(nullable = false, name = "current_virtual_date")
	private LocalDate currentVirtualDate;

	@Builder
	private DemoChallenge(Long userId, HomecareRoutine homecareRoutine,
		String title, LocalDate startDate) {
		this.userId = userId;
		this.homecareRoutine = homecareRoutine;
		this.title = title;
		this.startDate = startDate;
		this.endDate = startDate.plusDays(this.totalDays - 1);
		this.currentVirtualDate = startDate;
	}

	/**
	 * 챌린지 루틴 생성 팩토리 메서드
	 * @param routineNames 루틴명 리스트
	 * @return 생성된 챌린지 루틴 리스트 (루틴명 개수 × 7일)
	 */
	public List<DemoChallengeRoutine> createChallengeRoutines(List<String> routineNames) {
		List<DemoChallengeRoutine> routines = new ArrayList<>();

		for (int day = 0; day < totalDays; day++) {
			LocalDate scheduledDate = startDate.plusDays(day);

			for (String routineName : routineNames) {
				routines.add(DemoChallengeRoutine.builder()
					.demoChallenge(this)
					.name(routineName)
					.scheduledDate(scheduledDate)
					.build());
			}
		}

		return routines;
	}

	/**
	 * 다음 날로 진행 (데모용)
	 * @throws ChallengeException 종료일을 넘는 경우
	 */
	public void advanceDay() {
		LocalDate nextDay = currentVirtualDate.plusDays(1);

		if (nextDay.isAfter(endDate)) {
			throw new ChallengeException(
				ChallengeErrorCode.ROUTINE_OUT_OF_CHALLENGE_PERIOD
			);
		}

		this.currentVirtualDate = nextDay;
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
	 * @return 현재 일차 (1부터 시작)
	 */
	public int getCurrentDay() {
		if (currentVirtualDate.isBefore(startDate)) {
			return 0;
		}

		if (currentVirtualDate.isAfter(endDate)) {
			return totalDays;
		}

		return (int) ChronoUnit.DAYS.between(startDate, currentVirtualDate) + 1;
	}
}
