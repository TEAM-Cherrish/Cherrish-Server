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
import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchPort;
import com.sopt.cherrish.domain.procedure.domain.port.ProcedureSearchResult;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureResponseDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureWorryResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcedureService {

	private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);
	private final ProcedureRepository procedureRepository;
	private final ProcedureWorryRepository procedureWorryRepository;
	private final ProcedureSearchPort procedureSearchPort;

	public ProcedureListResponseDto searchProcedures(String keyword, Long worryId) {
		SearchResult searchResult = searchProceduresInternal(keyword, worryId);
		Map<Long, List<ProcedureWorryResponseDto>> worriesByProcedureId = fetchWorriesByProcedure(
			searchResult.procedures(),
			worryId
		);

		List<ProcedureResponseDto> responses = searchResult.procedures().stream()
			.map(procedure -> ProcedureResponseDto.from(
				procedure,
				worriesByProcedureId.getOrDefault(procedure.getId(), List.of())
			))
			.toList();

		// ES 검색 결과는 관련도순 유지, 그 외에는 한글 가나다순 정렬
		if (searchResult.isEsResult()) {
			return ProcedureListResponseDto.of(responses);
		}

		return ProcedureListResponseDto.of(
			responses.stream()
				.sorted((p1, p2) -> KOREAN_COLLATOR.compare(p1.name(), p2.name()))
				.toList()
		);
	}

	private SearchResult searchProceduresInternal(String keyword, Long worryId) {
		// 키워드가 없으면 기존 QueryDSL 검색 사용
		if (keyword == null || keyword.isBlank()) {
			return new SearchResult(procedureRepository.searchProcedures(null, worryId), false);
		}

		ProcedureSearchResult searchResult = procedureSearchPort.searchByKeyword(keyword);
		if (searchResult.isAvailable()) {
			List<Long> procedureIds = searchResult.procedureIds();
			if (procedureIds.isEmpty()) {
				return new SearchResult(List.of(), true);
			}

			// ES 순서를 유지하면서 DB 조회
			List<Procedure> procedures = procedureRepository.findByIdInAndWorryId(procedureIds, worryId);
			List<Procedure> orderedProcedures = reorderByEsResult(procedures, procedureIds);
			return new SearchResult(orderedProcedures, true);
		}

		// 폴백: 기존 QueryDSL LIKE 검색
		log.info("ES 사용 불가, DB LIKE 검색으로 폴백: keyword={}", keyword);
		return new SearchResult(procedureRepository.searchProcedures(keyword, worryId), false);
	}

	/**
	 * ES 검색 결과 순서대로 Procedure 목록을 재정렬
	 */
	private List<Procedure> reorderByEsResult(List<Procedure> procedures, List<Long> esOrderedIds) {
		Map<Long, Procedure> procedureMap = procedures.stream()
			.collect(Collectors.toMap(Procedure::getId, p -> p, (existing, replacement) -> existing));

		return esOrderedIds.stream()
			.map(procedureMap::get)
			.filter(p -> p != null)
			.toList();
	}

	private record SearchResult(List<Procedure> procedures, boolean isEsResult) {
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
