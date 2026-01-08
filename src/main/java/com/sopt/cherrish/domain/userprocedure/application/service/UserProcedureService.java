package com.sopt.cherrish.domain.userprocedure.application.service;

import java.util.List;
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
			Set<Long> missingIds = procedureIds.stream()
				.filter(id -> !foundIds.contains(id))
				.collect(Collectors.toSet());

			log.warn("Procedures not found: {}", missingIds);
			throw new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND);
		}

		return procedures;
	}
}
