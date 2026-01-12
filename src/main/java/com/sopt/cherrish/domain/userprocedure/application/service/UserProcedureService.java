package com.sopt.cherrish.domain.userprocedure.application.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.exception.ProcedureException;
import com.sopt.cherrish.domain.user.domain.repository.UserRepository;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;
import com.sopt.cherrish.domain.user.exception.UserException;
import com.sopt.cherrish.domain.maindashboard.domain.model.ProcedurePhase;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.RecentProcedureResponseDto;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.UpcomingProcedureResponseDto;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.domain.userprocedure.domain.repository.UserProcedureRepository;
import com.sopt.cherrish.domain.userprocedure.domain.vo.DowntimePeriod;
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

	private final UserRepository userRepository;
	private final ProcedureRepository procedureRepository;
	private final UserProcedureRepository userProcedureRepository;

	@Transactional
	public UserProcedureCreateResponseDto createUserProcedures(Long userId, UserProcedureCreateRequestDto request) {
		User user = getUserOrThrow(userId);

		List<Procedure> procedures = getProceduresOrThrow(request.getProcedures());
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
			.map(UserProcedureCreateRequestItemDto::getProcedureId)
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
	 * 최근 시술 조회 (가장 최근 날짜의 모든 시술, COMPLETED 제외)
	 * 정렬 순서: 1) ProcedurePhase (SENSITIVE > CAUTION > RECOVERY) 2) 최근 시술부터
	 * @param userId 사용자 ID
	 * @param today 오늘 날짜
	 * @return 최근 시술 DTO 리스트
	 */
	public List<RecentProcedureResponseDto> findRecentProcedures(Long userId, LocalDate today) {
		return userProcedureRepository
			.findProceduresOnMostRecentDate(userId, today)
			.stream()
			.map(up -> {
				ProcedurePhase phase = calculatePhase(up, today);
				if (phase == ProcedurePhase.COMPLETED) {
					return null;
				}
				return new RecentProcedureWithPhase(
					RecentProcedureResponseDto.from(up, today, phase),
					phase,
					up.getScheduledAt()
				);
			})
			.filter(Objects::nonNull)
			.sorted(Comparator
				.comparing((RecentProcedureWithPhase r) -> r.phase) // 민감기 > 주의기 > 회복기
				.thenComparing(r -> r.scheduledAt, Comparator.reverseOrder()) // 최근 시술부터
			)
			.map(r -> r.dto)
			.toList();
	}

	/**
	 * 정렬을 위한 내부 클래스
	 */
	private record RecentProcedureWithPhase(
		RecentProcedureResponseDto dto,
		ProcedurePhase phase,
		java.time.LocalDateTime scheduledAt
	) {
	}

	/**
	 * 다가오는 시술 조회 (날짜별 그룹핑, 가장 가까운 N개 날짜)
	 * @param userId 사용자 ID
	 * @param today 오늘 날짜
	 * @param limitDates 조회할 최대 날짜 개수
	 * @return 날짜별 예정 시술 DTO 리스트
	 */
	public List<UpcomingProcedureResponseDto> findUpcomingProceduresGroupedByDate(
		Long userId, LocalDate today, int limitDates
	) {
		LocalDate tomorrow = today.plusDays(1);

		// 내일부터 시작하는 모든 미래 시술 조회
		List<UserProcedure> allUpcoming = userProcedureRepository
			.findUpcomingProceduresGroupedByDate(userId, tomorrow);

		// 날짜별로 그룹핑
		Map<LocalDate, List<UserProcedure>> groupedByDate = allUpcoming.stream()
			.collect(Collectors.groupingBy(
				up -> up.getScheduledAt().toLocalDate()
			));

		// 가장 가까운 N개 날짜만 선택, 각 날짜마다 다운타임 가장 긴 시술 찾기
		return groupedByDate.entrySet().stream()
			.sorted(Map.Entry.comparingByKey()) // 날짜 오름차순
			.limit(limitDates) // 최대 N개 날짜
			.map(entry -> {
				LocalDate date = entry.getKey();
				List<UserProcedure> procedures = entry.getValue();

				// 다운타임 가장 긴 시술 찾기
				UserProcedure longest = procedures.stream()
					.max(Comparator.comparing(UserProcedure::getDowntimeDays))
					.orElseThrow();

				return UpcomingProcedureResponseDto.of(
					date,
					longest.getProcedure().getName(),
					procedures.size(),
					today
				);
			})
			.toList();
	}

	/**
	 * UserProcedure의 현재 다운타임 단계 계산
	 * @param userProcedure 사용자 시술
	 * @param today 오늘 날짜
	 * @return 다운타임 단계
	 */
	private ProcedurePhase calculatePhase(UserProcedure userProcedure, LocalDate today) {
		DowntimePeriod period = userProcedure.calculateDowntimePeriod();
		return ProcedurePhase.calculate(period, today);
	}
}
