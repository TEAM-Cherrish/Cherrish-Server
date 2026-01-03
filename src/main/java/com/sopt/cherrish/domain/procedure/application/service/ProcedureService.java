package com.sopt.cherrish.domain.procedure.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcedureService {

	private final ProcedureRepository procedureRepository;

	public ProcedureListResponseDto searchProcedures(String keyword, Long worryId) {
		List<Procedure> procedures = procedureRepository.searchProcedures(keyword, worryId);

		List<ProcedureResponseDto> procedureDtos = procedures.stream()
			.map(ProcedureResponseDto::from)
			.toList();

		return ProcedureListResponseDto.of(procedureDtos);
	}
}
