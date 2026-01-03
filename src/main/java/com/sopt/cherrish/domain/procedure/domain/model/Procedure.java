package com.sopt.cherrish.domain.procedure.domain.model;

import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "procedures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Procedure extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 50)
	private String category;

	@Column(nullable = false)
	private int minDowntimeDays;

	@Column(nullable = false)
	private int maxDowntimeDays;

	@Builder
	private Procedure(String name, String category, int minDowntimeDays, int maxDowntimeDays) {
		validateDowntime(minDowntimeDays, maxDowntimeDays);
		this.name = name;
		this.category = category;
		this.minDowntimeDays = minDowntimeDays;
		this.maxDowntimeDays = maxDowntimeDays;
	}

	/**
	 * 다운타임 유효성 검증
	 * - 다운타임은 0 이상이어야 함
	 * - 최소 다운타임은 최대 다운타임보다 작거나 같아야 함
	 *
	 * @throws ProcedureException 다운타임 값이 유효하지 않은 경우
	 */
	private void validateDowntime(int minDowntimeDays, int maxDowntimeDays) {
		if (minDowntimeDays < 0 || maxDowntimeDays < 0) {
			throw new ProcedureException(ProcedureErrorCode.INVALID_DOWNTIME_VALUE);
		}
		if (minDowntimeDays > maxDowntimeDays) {
			throw new ProcedureException(ProcedureErrorCode.INVALID_DOWNTIME_RANGE);
		}
	}
}
