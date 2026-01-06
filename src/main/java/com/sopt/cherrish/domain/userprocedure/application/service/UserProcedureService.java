package com.sopt.cherrish.domain.userprocedure.application.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProcedureService {

	private final UserRepository userRepository;
	private final ProcedureRepository procedureRepository;
	private final UserProcedureRepository userProcedureRepository;

	@Transactional
	public UserProcedureCreateResponseDto createUserProcedures(Long userId, UserProcedureCreateRequestDto request) {
		validateUserExists(userId);

		List<Procedure> procedures = getProceduresOrThrow(request.getProcedures());
		List<UserProcedure> userProcedures = request.toEntities(userId, procedures);
		List<UserProcedure> savedProcedures = userProcedureRepository.saveAll(userProcedures);
		return UserProcedureCreateResponseDto.from(savedProcedures);
	}

	private void validateUserExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}
	}

	private List<Procedure> getProceduresOrThrow(List<UserProcedureCreateRequestItemDto> items) {
		Set<Long> procedureIds = items.stream()
			.map(UserProcedureCreateRequestItemDto::getProcedureId)
			.collect(Collectors.toSet());

		List<Procedure> procedures = procedureRepository.findAllById(procedureIds);
		if (procedures.size() != procedureIds.size()) {
			throw new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND);
		}

		return procedures;
	}
}
