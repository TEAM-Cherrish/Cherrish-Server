package com.sopt.cherrish.domain.calendar.application.service;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.calendar.domain.service.CalendarValidator;
import com.sopt.cherrish.domain.calendar.domain.service.DowntimeCalculator;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDateDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDto;
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
	private final CalendarValidator calendarValidator;

	public CalendarResponseDto getCalendar(Long userId, int year, int month) {
		// 도메인 범위 검증
		calendarValidator.validateYearMonth(year, month);

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

		Map<LocalDate, List<ProcedureEventDto>> eventsByDate = userProcedures.stream()
				.map(this::convertToProcedureEventDto)
				.collect(Collectors.groupingBy(event -> event.scheduledAt().toLocalDate()));

		List<CalendarDateDto> dates = eventsByDate.entrySet().stream()
				.map(entry -> CalendarDateDto.of(entry.getKey(), entry.getValue()))
				.sorted((a, b) -> a.date().compareTo(b.date()))
				.toList();

		return CalendarResponseDto.of(year, month, dates);
	}

	private ProcedureEventDto convertToProcedureEventDto(UserProcedure userProcedure) {
		// 다운타임 일수 결정 (개인 설정이 있으면 우선, 없으면 시술 마스터의 최대값)
		Integer customDowntime = userProcedure.getDowntimeDays();
		Integer procedureMaxDowntime = userProcedure.getProcedure().getMaxDowntimeDays();
		int downtimeDays = customDowntime != null ? customDowntime :
				(procedureMaxDowntime != null ? procedureMaxDowntime : 0);

		// 다운타임 기간 계산
		LocalDate scheduledDate = userProcedure.getScheduledAt().toLocalDate();
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, scheduledDate);

		return ProcedureEventDto.from(userProcedure, periods, downtimeDays);
	}
}
