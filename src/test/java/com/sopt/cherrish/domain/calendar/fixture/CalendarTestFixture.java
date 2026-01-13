package com.sopt.cherrish.domain.calendar.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.sopt.cherrish.domain.calendar.domain.model.CalendarEventType;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventResponseDto;
import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;

public class CalendarTestFixture {

	private CalendarTestFixture() {
	}

	public static User createMockUser(String name, int age) {
		return User.builder()
			.name(name)
			.age(age)
			.build();
	}

	public static Procedure createMockProcedure(String name) {
		return Procedure.builder()
			.name(name)
			.build();
	}

	public static UserProcedure createUserProcedure(
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		int downtimeDays
	) {
		return UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(downtimeDays)
			.build();
	}

	public static CalendarDailyResponseDto createDailyResponseWithSingleEvent(
		Long userProcedureId,
		Long procedureId,
		String procedureName,
		LocalDateTime scheduledAt,
		int downtimeDays
	) {
		ProcedureEventResponseDto event = ProcedureEventResponseDto.builder()
			.type(CalendarEventType.PROCEDURE)
			.userProcedureId(userProcedureId)
			.procedureId(procedureId)
			.name(procedureName)
			.scheduledAt(scheduledAt)
			.downtimeDays(downtimeDays)
			.build();

		return CalendarDailyResponseDto.from(List.of(event));
	}

	public static ProcedureEventDowntimeResponseDto createDowntimeResponse(
		Long userProcedureId,
		LocalDateTime scheduledAt,
		int downtimeDays,
		List<LocalDate> sensitiveDays,
		List<LocalDate> cautionDays,
		List<LocalDate> recoveryDays
	) {
		return ProcedureEventDowntimeResponseDto.builder()
			.userProcedureId(userProcedureId)
			.scheduledAt(scheduledAt)
			.downtimeDays(downtimeDays)
			.sensitiveDays(sensitiveDays)
			.cautionDays(cautionDays)
			.recoveryDays(recoveryDays)
			.build();
	}
}
