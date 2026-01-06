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
	 * 완료한 루틴 개수 기반 체리 레벨 계산
	 * 총 루틴을 4등분하여 각 레벨 할당
	 * 예: 총 200개 → 레벨1(0~50), 레벨2(51~100), 레벨3(101~150), 레벨4(151~200)
	 * @return 체리 레벨 (1-4)
	 */
	public int calculateCherryLevel() {
		if (totalRoutineCount == 0) {
			return 1;
		}

		int levelSize = totalRoutineCount / 4;  // 각 레벨의 크기

		// 레벨 크기가 0이면 모든 구간을 채운 것으로 간주
		if (levelSize == 0) {
			return 4;
		}

		// 수식으로 단순화: (completedCount / levelSize) + 1, 최대 4
		return Math.min(4, (completedCount / levelSize) + 1);
	}

	/**
	 * 현재 진행률에 따라 체리 레벨 업데이트
	 */
	public void updateCherryLevel() {
		this.cherryLevel = calculateCherryLevel();
	}

	/**
	 * 현재 레벨 구간 내에서의 진척도 계산 (0-100%)
	 * 예: 총 200개 중 75개 완료 시 레벨2(51~100)에서 50% 진척
	 * @return 현재 레벨 내 진척도 (%)
	 */
	public double getProgressToNextLevel() {
		if (totalRoutineCount == 0) {
			return 0.0;
		}

		int currentLevel = this.cherryLevel;
		int levelSize = totalRoutineCount / 4;  // 각 레벨의 크기

		// 최대 레벨이면 100% 반환
		if (currentLevel >= 4) {
			return 100.0;
		}

		// 레벨 구간 크기가 0이면 0% 반환
		if (levelSize == 0) {
			return 0.0;
		}

		// 현재 레벨의 시작점 (개수)
		int levelStart = (currentLevel - 1) * levelSize;

		// 현재 레벨 구간 내에서 완료한 개수
		int progressInLevel = completedCount - levelStart;

		double percentage = ((double)progressInLevel / levelSize) * 100.0;
		return Math.round(percentage * 10.0) / 10.0;  // 소수점 1자리까지 반올림
	}
}
