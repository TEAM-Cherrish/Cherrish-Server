package com.sopt.cherrish.domain.challenge.core.domain.model;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeStatistics extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "challenge_id", nullable = false, unique = true)
	private Challenge challenge;

	@Column(nullable = false, name = "completed_count")
	private Integer completedCount = 0;

	@Column(nullable = false, name = "total_routine_count")
	private Integer totalRoutineCount;

	@Column(nullable = false, name = "cherry_level")
	private Integer cherryLevel = 1;

	@Builder
	private ChallengeStatistics(Challenge challenge, Integer totalRoutineCount) {
		this.challenge = challenge;
		this.completedCount = 0;
		this.totalRoutineCount = totalRoutineCount;
		this.cherryLevel = 1;
	}

	public void incrementCompletedCount() {
		this.completedCount++;
	}

	public void decrementCompletedCount() {
		if (this.completedCount > 0) {
			this.completedCount--;
		}
	}

	public double getProgressPercentage() {
		if (totalRoutineCount == 0) {
			return 0.0;
		}
		double percentage = (double) completedCount / totalRoutineCount * 100;
		return Math.round(percentage * 10.0) / 10.0;  // 소수점 1자리까지 반올림
	}

	/**
	 * 완료 진행률 기반 체리 레벨 계산
	 *
	 * 레벨 구간:
	 * - 레벨 1:   0% ~ 24.99%
	 * - 레벨 2:  25% ~ 49.99%
	 * - 레벨 3:  50% ~ 74.99%
	 * - 레벨 4:  75% ~ 100%
	 *
	 * @return 체리 레벨 (1-4)
	 */
	public int calculateCherryLevel() {
		if (totalRoutineCount == 0) {
			return 1;  // 기본값
		}

		double progressPercentage = getProgressPercentage();

		if (progressPercentage < 25.0) {
			return 1;
		}
		if (progressPercentage < 50.0) {
			return 2;
		}
		if (progressPercentage < 75.0) {
			return 3;
		}
		return 4;
	}

	/**
	 * 현재 진행률에 따라 체리 레벨 업데이트
	 */
	public void updateCherryLevel() {
		this.cherryLevel = calculateCherryLevel();
	}

	/**
	 * 현재 레벨 구간 내에서의 진척도 계산 (0-100%)
	 *
	 * 예: 전체 진행률 37.5% → 레벨 2(25-50% 구간)에서 50% 진척
	 *
	 * @return 현재 레벨 내 진척도 (%)
	 */
	public double getProgressToNextLevel() {
		if (totalRoutineCount == 0) {
			return 0.0;
		}

		double progressPercentage = getProgressPercentage();
		int currentLevel = this.cherryLevel;

		// 최대 레벨이면 100% 반환
		if (currentLevel >= 4 || progressPercentage >= 100.0) {
			return 100.0;
		}

		// 각 레벨의 시작 진행률
		double levelStartPercentage = (currentLevel - 1) * 25.0;

		// 현재 레벨 구간 내 진행률
		double progressInLevel = progressPercentage - levelStartPercentage;

		// 레벨 구간(25%) 대비 진척도
		double progressToNext = (progressInLevel / 25.0) * 100.0;

		return Math.round(progressToNext * 10.0) / 10.0;  // 소수점 1자리까지 반올림
	}
}
