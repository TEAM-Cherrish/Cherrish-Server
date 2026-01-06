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

	@Builder
	private ChallengeStatistics(Challenge challenge, Integer totalRoutineCount) {
		this.challenge = challenge;
		this.completedCount = 0;
		this.totalRoutineCount = totalRoutineCount;
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
}
