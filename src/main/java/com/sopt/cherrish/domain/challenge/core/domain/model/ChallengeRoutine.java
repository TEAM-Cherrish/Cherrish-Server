package com.sopt.cherrish.domain.challenge.core.domain.model;

import java.time.LocalDate;

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
		this.challenge = challenge;
		this.name = name;
		this.scheduledDate = scheduledDate;
	}

	public void complete() {
		this.isComplete = true;
	}

	/**
	 * 완료 상태 토글
	 * @return 새로운 완료 상태 (true: 완료, false: 미완료)
	 */
	public boolean toggleCompletion() {
		this.isComplete = !this.isComplete;
		return this.isComplete;
	}

	public boolean isScheduledFor(LocalDate date) {
		return this.scheduledDate.equals(date);
	}
}
