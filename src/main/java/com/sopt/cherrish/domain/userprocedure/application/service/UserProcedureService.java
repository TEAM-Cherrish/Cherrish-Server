package com.sopt.cherrish.domain.userprocedure.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
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
import com.sopt.cherrish.domain.userprocedure.presentation.dto.response.UserProcedureListResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProcedureService {

	private final UserRepository userRepository;
	private final ProcedureRepository procedureRepository;
	private final UserProcedureRepository userProcedureRepository;

	@Transactional
	public UserProcedureListResponseDto createUserProcedures(Long userId, UserProcedureCreateRequestDto request) {
		validateUserExists(userId);

		List<UserProcedure> userProcedures = new ArrayList<>();

		for (UserProcedureCreateRequestItemDto item : request.getProcedures()) {
			Procedure procedure = procedureRepository.findById(item.getProcedureId())
				.orElseThrow(() -> new ProcedureException(ProcedureErrorCode.PROCEDURE_NOT_FOUND));

			UserProcedure userProcedure = UserProcedure.builder()
				.userId(userId)
				.procedure(procedure)
				.scheduledAt(request.getScheduledAt())
				.downtimeDays(item.getDowntimeDays())
				.build();
			userProcedures.add(userProcedure);
		}

		List<UserProcedure> savedProcedures = userProcedureRepository.saveAll(userProcedures);
		return UserProcedureListResponseDto.from(savedProcedures);
	}

	private void validateUserExists(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new UserException(UserErrorCode.USER_NOT_FOUND);
		}
	}
}
