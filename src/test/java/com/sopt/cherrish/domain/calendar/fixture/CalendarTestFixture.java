package com.sopt.cherrish.domain.calendar.fixture;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.sopt.cherrish.domain.calendar.domain.model.CalendarEventType;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventResponseDto;
import com.sopt.cherrish.domain.auth.domain.model.SocialProvider;
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
			.socialProvider(SocialProvider.KAKAO)
			.socialId("test_social_id")
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
		return createUserProcedure(user, procedure, scheduledAt, downtimeDays, null);
	}

	public static UserProcedure createUserProcedure(
		User user,
		Procedure procedure,
		LocalDateTime scheduledAt,
		int downtimeDays,
		LocalDate recoveryTargetDate
	) {
		return UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(downtimeDays)
			.recoveryTargetDate(recoveryTargetDate)
			.build();
	}

	public static CalendarDailyResponseDto createDailyResponseWithSingleEvent(
		Long userProcedureId,
		Long procedureId,
		String procedureName,
		LocalDateTime scheduledAt,
		int downtimeDays
	) {
		ProcedureEventResponseDto event = new ProcedureEventResponseDto(
			CalendarEventType.PROCEDURE,
			userProcedureId,
			procedureId,
			procedureName,
			scheduledAt,
			downtimeDays
		);

		return CalendarDailyResponseDto.from(List.of(event));
	}

	public static ProcedureEventDowntimeResponseDto createDowntimeResponse(
		Long userProcedureId,
		LocalDateTime scheduledAt,
		int downtimeDays,
		LocalDate recoveryTargetDate,
		List<LocalDate> sensitiveDays,
		List<LocalDate> cautionDays,
		List<LocalDate> recoveryDays
	) {
		return new ProcedureEventDowntimeResponseDto(
			userProcedureId,
			scheduledAt,
			downtimeDays,
			recoveryTargetDate,
			sensitiveDays,
			cautionDays,
			recoveryDays
		);
	}
}
