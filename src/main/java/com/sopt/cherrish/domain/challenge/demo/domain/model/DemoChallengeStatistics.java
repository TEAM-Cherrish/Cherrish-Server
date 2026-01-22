package com.sopt.cherrish.domain.challenge.demo.domain.model;

import com.sopt.cherrish.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demo_challenge_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoChallengeStatistics extends BaseTimeEntity {

	private static final double LEVEL_2_THRESHOLD = 25.0;
	private static final double LEVEL_3_THRESHOLD = 50.0;
	private static final double LEVEL_4_THRESHOLD = 75.0;
	private static final double LEVEL_RANGE = 25.0;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Version
	@Column(nullable = false)
	private Long version = 0L;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "demo_challenge_id", nullable = false, unique = true)
	private DemoChallenge demoChallenge;

	@Column(nullable = false, name = "completed_count")
	private Integer completedCount = 0;

	@Column(nullable = false, name = "total_routine_count")
	private Integer totalRoutineCount;

	@Column(nullable = false, name = "cherry_level")
	private Integer cherryLevel = 0;

	@Builder
	private DemoChallengeStatistics(DemoChallenge demoChallenge, Integer totalRoutineCount) {
		this.demoChallenge = demoChallenge;
		this.completedCount = 0;
		this.totalRoutineCount = totalRoutineCount;
		this.cherryLevel = 0;
	}

	/**
	 * 완료 개수 직접 설정 (재계산용)
	 */
	public void setCompletedCount(int count) {
		if (count < 0) {
			this.completedCount = 0;
		} else if (count > this.totalRoutineCount) {
			this.completedCount = this.totalRoutineCount;
		} else {
			this.completedCount = count;
		}
	}

	/**
	 * 체리 레벨 업데이트
	 */
	public void updateCherryLevel() {
		this.cherryLevel = calculateCherryLevel();
	}

	public int getProgressPercentage() {
		if (totalRoutineCount == 0) {
			return 0;
		}
		return (int) Math.round((double) completedCount / totalRoutineCount * 100);
	}

	/**
	 * 완료 진행률 기반 체리 레벨 계산
	 * - 레벨 0: completedCount == 0 (몽롱체리)
	 * - 레벨 1: 1개 이상, 0% ~ 24.99%
	 * - 레벨 2: 25% ~ 49.99%
	 * - 레벨 3: 50% ~ 74.99%
	 * - 레벨 4: 75% ~ 100%
	 */
	public int calculateCherryLevel() {
		if (completedCount == 0) {
			return 0;
		}

		double progressPercentage = getProgressPercentage();

		if (progressPercentage < LEVEL_2_THRESHOLD) {
			return 1;
		}
		if (progressPercentage < LEVEL_3_THRESHOLD) {
			return 2;
		}
		if (progressPercentage < LEVEL_4_THRESHOLD) {
			return 3;
		}
		return 4;
	}

	/**
	 * 현재 레벨 구간 내에서의 진척도 계산 (0-100%)
	 */
	public double getProgressToNextLevel() {
		if (totalRoutineCount == 0 || completedCount == 0) {
			return 0.0;
		}

		double progressPercentage = getProgressPercentage();
		int currentLevel = calculateCherryLevel();

		if (currentLevel >= 4 || progressPercentage >= 100.0) {
			return 100.0;
		}

		double levelStartPercentage = (currentLevel - 1) * LEVEL_RANGE;
		double progressInLevel = progressPercentage - levelStartPercentage;
		double progressToNext = (progressInLevel / LEVEL_RANGE) * 100.0;

		return Math.round(progressToNext * 10.0) / 10.0;
	}

	/**
	 * 다음 레벨까지 남은 루틴 개수 계산
	 * 레벨 0, 1일 때는 레벨 2(25%)까지 남은 개수 반환
	 * 레벨 4일 때는 100%까지 남은 루틴 개수를 반환
	 */
	public int getRemainingRoutinesToNextLevel() {
		double nextThreshold = switch (cherryLevel) {
			case 0, 1 -> LEVEL_2_THRESHOLD;
			case 2 -> LEVEL_3_THRESHOLD;
			case 3 -> LEVEL_4_THRESHOLD;
			default -> 100.0;
		};

		int requiredCompletedCount = (int) Math.ceil(totalRoutineCount * nextThreshold / 100.0);

		return Math.max(0, requiredCompletedCount - completedCount);
	}
}
