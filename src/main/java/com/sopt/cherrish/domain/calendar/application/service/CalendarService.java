package com.sopt.cherrish.domain.calendar.application.service;

import com.sopt.cherrish.domain.calendar.domain.model.UserProcedure;
import com.sopt.cherrish.domain.calendar.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.calendar.domain.service.DowntimeCalculator;
import com.sopt.cherrish.domain.calendar.domain.vo.DowntimePeriods;
import com.sopt.cherrish.domain.calendar.exception.CalendarErrorCode;
import com.sopt.cherrish.domain.calendar.exception.CalendarException;
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

	public CalendarResponseDto getCalendar(int year, int month) {

        // 도메인 범위 검증
        validateYearMonth(year, month);

        // TODO: 실제 사용자 인증 구현 후 수정
		Long userId = 1L;

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

    private void validateYearMonth(int year, int month) {
        if (year < 2000 || year > 2100) {
            throw new CalendarException(CalendarErrorCode.INVALID_YEAR_RANGE);
        }
        if (month < 1 || month > 12) {
            throw new CalendarException(CalendarErrorCode.INVALID_MONTH_RANGE);
        }
    }

	private ProcedureEventDto convertToProcedureEventDto(UserProcedure userProcedure) {

        if (userProcedure.getProcedure() == null) {
            throw new CalendarException(CalendarErrorCode.PROCEDURE_NOT_FOUND);
        }

        // 다운타임 일수 결정 (개인 설정이 있으면 우선, 없으면 시술 마스터의 최대값)
		Integer downtimeDays = userProcedure.getDowntimeDays() != null
				? userProcedure.getDowntimeDays()
				: userProcedure.getProcedure().getMaxDowntimeDays();

		// 다운타임 기간 계산
		LocalDate scheduledDate = userProcedure.getScheduledAt().toLocalDate();
		DowntimePeriods periods = downtimeCalculator.calculate(downtimeDays, scheduledDate);

		return ProcedureEventDto.from(userProcedure, periods, downtimeDays);
	}
}
