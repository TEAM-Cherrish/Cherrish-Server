package com.sopt.cherrish.domain.challenge.demo.domain.model;

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
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "demo_challenge_routines", indexes = {
	@Index(name = "idx_demo_challenge_scheduled", columnList = "demo_challenge_id,scheduled_date"),
	@Index(name = "idx_demo_challenge_complete", columnList = "demo_challenge_id,is_complete")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoChallengeRoutine extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	@Version
	private Long version = 0L;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "demo_challenge_id", nullable = false)
	private DemoChallenge demoChallenge;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, name = "scheduled_date")
	private LocalDate scheduledDate;

	@Column(nullable = false, name = "is_complete")
	private Boolean isComplete = false;

	@Builder
	private DemoChallengeRoutine(DemoChallenge demoChallenge, String name, LocalDate scheduledDate) {
		this.demoChallenge = demoChallenge;
		this.name = name;
		this.scheduledDate = scheduledDate;
	}

	/**
	 * 완료 상태 토글 (데모 - 통계 즉시 업데이트 안 됨)
	 */
	public void toggleCompletion() {
		this.isComplete = !this.isComplete;
	}
}
