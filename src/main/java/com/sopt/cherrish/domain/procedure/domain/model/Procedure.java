package com.sopt.cherrish.domain.procedure.domain.model;

import com.sopt.cherrish.domain.calendar.exception.CalendarErrorCode;
import com.sopt.cherrish.domain.calendar.exception.CalendarException;
import com.sopt.cherrish.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
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
	private Integer minDowntimeDays;

	@Column(nullable = false)
	private Integer maxDowntimeDays;

	@Builder
	private Procedure(String name, String category, Integer minDowntimeDays, Integer maxDowntimeDays) {
        validateDowntimeRange(minDowntimeDays, maxDowntimeDays);

        this.name = name;
		this.category = category;
		this.minDowntimeDays = minDowntimeDays != null ? minDowntimeDays : 0;
		this.maxDowntimeDays = maxDowntimeDays != null ? maxDowntimeDays : 0;
	}

    private void validateDowntimeRange(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new CalendarException(CalendarErrorCode.INVALID_DOWNTIME_RANGE);
        }
    }
}
