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
		return (double) completedCount / totalRoutineCount * 100;
	}

	/**
	 * 진행률 기반 체리 레벨 계산
	 * @return 체리 레벨 (1-4)
	 */
	public int calculateCherryLevel() {
		double progress = getProgressPercentage();
		if (progress <= 25.0) {
			return 1;
		}
		if (progress <= 50.0) {
			return 2;
		}
		if (progress <= 75.0) {
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
}
