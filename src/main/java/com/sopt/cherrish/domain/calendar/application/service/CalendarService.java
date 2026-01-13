package com.sopt.cherrish.domain.calendar.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarDailyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.CalendarMonthlyResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventDowntimeResponseDto;
import com.sopt.cherrish.domain.calendar.presentation.dto.response.ProcedureEventResponseDto;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureErrorCode;
import com.sopt.cherrish.domain.userprocedure.exception.UserProcedureException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

	private final UserProcedureRepository userProcedureRepository;
	private final UserRepository userRepository;

	/**
	 * 월별 캘린더 조회
	 * @param userId 사용자 ID
	 * @param year 연도
	 * @param month 월
	 * @return 일자별 시술 개수
	 */
	public CalendarMonthlyResponseDto getMonthlyCalendar(Long userId, int year, int month) {
		// 사용자 존재 여부 확인
		validateUserExists(userId);

		// 월별 시술 개수 조회
		Map<Integer, Long> dailyProcedureCounts = userProcedureRepository.findMonthlyProcedureCounts(userId, year,
			month);

		return CalendarMonthlyResponseDto.from(dailyProcedureCounts);
	}

	/**
	 * 일자별 시술 상세 조회
	 * @param userId 사용자 ID
	 * @param date 날짜
	 * @return 시술 이벤트 목록
	 */
	public CalendarDailyResponseDto getDailyCalendar(Long userId, LocalDate date) {
		// 사용자 존재 여부 확인
		validateUserExists(userId);

		// 특정 날짜의 시술 목록 조회
		List<UserProcedure> userProcedures = userProcedureRepository.findDailyProcedures(userId, date);

		// UserProcedure -> ProcedureEventResponseDto 변환
		List<ProcedureEventResponseDto> events = userProcedures.stream()
			.map(ProcedureEventResponseDto::from)
			.toList();

		return CalendarDailyResponseDto.from(events);
	}

	/**
	 * 시술 다운타임 상세 조회
	 * @param userId 사용자 ID
	 * @param userProcedureId 사용자 시술 일정 ID
	 * @return 시술 다운타임 상세 정보
	 */
	public ProcedureEventDowntimeResponseDto getEventDowntime(Long userId, Long userProcedureId) {
		// 사용자 존재 여부 확인
		validateUserExists(userId);

		// 시술 일정 단건 조회
		UserProcedure userProcedure = userProcedureRepository
			.findByIdAndUserIdWithProcedure(userProcedureId, userId)
			.orElseThrow(() -> new UserProcedureException(UserProcedureErrorCode.USER_PROCEDURE_NOT_FOUND));

		return ProcedureEventDowntimeResponseDto.from(userProcedure);
	}

	private void validateUserExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}
	}
}
