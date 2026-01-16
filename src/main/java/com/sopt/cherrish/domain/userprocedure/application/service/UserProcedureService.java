package com.sopt.cherrish.domain.userprocedure.application.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.userprocedure.domain.model.ProcedurePhase;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.request.UserProcedureCreateRequestItemDto;
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureCreateResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProcedureService {

	private static final int MAX_DOWNTIME_DAYS = 30;

	private final UserRepository userRepository;
	private final ProcedureRepository procedureRepository;
	private final UserProcedureRepository userProcedureRepository;

	@Transactional
	public UserProcedureCreateResponseDto createUserProcedures(Long userId, UserProcedureCreateRequestDto request) {
		User user = getUserOrThrow(userId);

		List<Procedure> procedures = getProceduresOrThrow(request.procedures());
		List<UserProcedure> userProcedures = request.toEntities(user, procedures);
		List<UserProcedure> savedProcedures = userProcedureRepository.saveAll(userProcedures);
		return UserProcedureCreateResponseDto.from(savedProcedures);
	}

	private User getUserOrThrow(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
	}

	private List<Procedure> getProceduresOrThrow(List<UserProcedureCreateRequestItemDto> items) {
		Set<Long> procedureIds = items.stream()
			.map(UserProcedureCreateRequestItemDto::procedureId)
			.collect(Collectors.toSet());

		List<Procedure> procedures = procedureRepository.findAllById(procedureIds);
		if (procedures.size() != procedureIds.size()) {
			Set<Long> foundIds = procedures.stream()
				.map(Procedure::getId)
				.collect(Collectors.toSet());

			Set<Long> missingIds = new HashSet<>(procedureIds);
			missingIds.removeAll(foundIds);

			log.warn("존재하지 않는 시술 ID: {}", missingIds);
			throw new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND);
		}

		return procedures;
	}

	/**
	 * 다운타임 진행 중인 모든 시술 조회 (COMPLETED 제외, 정렬됨)
	 * 정렬 순서: 1) ProcedurePhase (SENSITIVE > CAUTION > RECOVERY) 2) 같은 Phase면 최근 시술부터
	 * @param userId 사용자 ID
	 * @param today 오늘 날짜
	 * @return 정렬된 시술 엔티티 리스트
	 */
	public List<UserProcedure> findRecentProcedures(Long userId, LocalDate today) {
		LocalDate fromDate = today.minusDays(MAX_DOWNTIME_DAYS);

		// 최근 다운타임 기간 내의 과거 시술 조회
		return userProcedureRepository
			.findAllPastProcedures(userId, fromDate, today)
			.stream()
			.filter(up -> up.calculateCurrentPhase(today) != ProcedurePhase.COMPLETED)
			.sorted(Comparator
				.comparing((UserProcedure up) -> up.calculateCurrentPhase(today))
				.thenComparing(UserProcedure::getScheduledAt, Comparator.reverseOrder())
			)
			.toList();
	}

	/**
	 * 다가오는 시술 조회 (날짜별 그룹핑, 가장 가까운 N개 날짜)
	 * @param userId 사용자 ID
	 * @param today 오늘 날짜
	 * @param limitDates 조회할 최대 날짜 개수
	 * @return 날짜별로 그룹핑된 시술 Map (날짜 오름차순)
	 */
	public Map<LocalDate, List<UserProcedure>> findUpcomingProceduresGroupedByDate(
		Long userId, LocalDate today, int limitDates
	) {
		LocalDate tomorrow = today.plusDays(1);

		// 내일부터 시작하는 모든 미래 시술 조회
		List<UserProcedure> allUpcoming = userProcedureRepository
			.findUpcomingProceduresGroupedByDate(userId, tomorrow);

		// 날짜별로 그룹핑
		Map<LocalDate, List<UserProcedure>> grouped = allUpcoming.stream()
			.collect(Collectors.groupingBy(
				up -> up.getScheduledAt().toLocalDate()
			));

		// 가장 가까운 N개 날짜 추출 (정렬된 상태)
		List<LocalDate> closestDates = grouped.keySet().stream()
			.sorted()
			.limit(limitDates)
			.toList();

		// 결과 Map 생성
		Map<LocalDate, List<UserProcedure>> result = new LinkedHashMap<>();
		for (LocalDate date : closestDates) {
			result.put(date, grouped.get(date));
		}
		return result;

	}
}
