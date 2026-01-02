package com.sopt.cherrish.domain.calendar.application.service;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.calendar.domain.service.DowntimeCalculator;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDateResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

	private final UserProcedureRepository userProcedureRepository;
	private final DowntimeCalculator downtimeCalculator;

	public CalendarResponseDto getCalendar(Long userId, int year, int month) {
		YearMonth yearMonth = YearMonth.of(year, month);
		LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
		LocalDateTime endDateTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

		List<UserProcedure> userProcedures = userProcedureRepository.findByUserIdAndScheduledAtBetween(
				userId,
				startDateTime,
				endDateTime
		);

        // 일정이 없는 경우 빈 결과를 즉시 반환
        if (userProcedures.isEmpty()) {
            return CalendarResponseDto.of(year, month, Collections.emptyList());
        }

		Map<LocalDate, List<ProcedureEventResponseDto>> eventsByDate = userProcedures.stream()
				.map(this::convertToProcedureEventDto)
				.collect(Collectors.groupingBy(event -> event.scheduledAt().toLocalDate()));

		List<CalendarDateResponseDto> dates = eventsByDate.entrySet().stream()
				.map(entry -> CalendarDateResponseDto.of(entry.getKey(), entry.getValue()))
				.sorted((a, b) -> a.date().compareTo(b.date()))
				.toList();

		return CalendarResponseDto.of(year, month, dates);
	}

	private ProcedureEventResponseDto convertToProcedureEventDto(UserProcedure userProcedure) {
		int downtimeDays = userProcedure.getDowntimeDays();
		LocalDate scheduledDate = userProcedure.getScheduledAt().toLocalDate();
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, scheduledDate);

		return ProcedureEventResponseDto.from(userProcedure, periods, downtimeDays);
	}
}
