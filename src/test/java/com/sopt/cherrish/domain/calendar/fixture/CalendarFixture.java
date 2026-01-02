package com.sopt.cherrish.domain.calendar.fixture;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarFixture {

	public static Procedure createProcedure(String name, String category, int minDowntimeDays, int maxDowntimeDays) {
		Procedure procedure = Procedure.builder()
				.name(name)
				.category(category)
				.minDowntimeDays(minDowntimeDays)
				.maxDowntimeDays(maxDowntimeDays)
				.build();

		setId(procedure, 1L);
		return procedure;
	}

	public static Procedure createDefaultProcedure() {
		return createProcedure("레이저 토닝", "레이저", 3, 7);
	}

	public static UserProcedure createUserProcedure(
			Long id,
			Long userId,
			Procedure procedure,
			LocalDateTime scheduledAt,
			Integer downtimeDays
	) {
		UserProcedure userProcedure = UserProcedure.builder()
				.userId(userId)
				.procedure(procedure)
				.scheduledAt(scheduledAt)
				.downtimeDays(downtimeDays)
				.build();

		setId(userProcedure, id);
		return userProcedure;
	}

	public static UserProcedure createUserProcedureWithoutCustomDowntime(
			Long id,
			Long userId,
			Procedure procedure,
			LocalDateTime scheduledAt
	) {
		return createUserProcedure(id, userId, procedure, scheduledAt, null);
	}

	public static DowntimePeriods createDefaultDowntimePeriods() {
		LocalDate baseDate = LocalDate.of(2025, 1, 15);
		return new DowntimePeriods(
				List.of(baseDate, baseDate.plusDays(1), baseDate.plusDays(2)),
				List.of(baseDate.plusDays(3), baseDate.plusDays(4)),
				List.of(baseDate.plusDays(5), baseDate.plusDays(6))
		);
	}

	private static <T> void setId(T entity, Long id) {
		try {
			Field idField = entity.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(entity, id);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("Failed to set id field", e);
		}
	}
}
