package com.sopt.cherrish.domain.procedure.application.service;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.model.ProcedureWorry;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureRepository;
import com.sopt.cherrish.domain.procedure.domain.repository.ProcedureWorryRepository;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureWorryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcedureService {

	private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);
	private final ProcedureRepository procedureRepository;
	private final ProcedureWorryRepository procedureWorryRepository;

	public ProcedureListResponseDto searchProcedures(String keyword, Long worryId) {
		List<Procedure> procedures = procedureRepository.searchProcedures(keyword, worryId);
		Map<Long, List<ProcedureWorryResponseDto>> worriesByProcedureId = fetchWorriesByProcedure(
			procedures,
			worryId
		);
		List<ProcedureResponseDto> responses = procedures.stream()
			.map(procedure -> ProcedureResponseDto.from(
				procedure,
				worriesByProcedureId.getOrDefault(procedure.getId(), List.of())
			))
			.toList();

		// DB의 한글 collation 설정과 무관하게 정확한 한글 정렬을 보장하기 위해 Java에서 정렬
		return ProcedureListResponseDto.of(
			responses.stream()
				.sorted((p1, p2) -> KOREAN_COLLATOR.compare(p1.name(), p2.name()))
				.toList()
		);
	}

	private Map<Long, List<ProcedureWorryResponseDto>> fetchWorriesByProcedure(
		List<Procedure> procedures,
		Long worryId
	) {
		List<Long> procedureIds = procedures.stream()
			.map(Procedure::getId)
			.toList();
		if (procedureIds.isEmpty()) {
			return Map.of();
		}

		List<ProcedureWorry> procedureWorries = worryId == null
			? procedureWorryRepository.findAllByProcedureIdInWithWorry(procedureIds)
			: procedureWorryRepository.findAllByProcedureIdInWithWorryAndWorryId(procedureIds, worryId);

		return procedureWorries.stream()
			.collect(Collectors.groupingBy(
				procedureWorry -> procedureWorry.getProcedure().getId(),
				Collectors.mapping(
					procedureWorry -> ProcedureWorryResponseDto.from(procedureWorry.getWorry()),
					Collectors.toList()
				)
			));
	}
}
