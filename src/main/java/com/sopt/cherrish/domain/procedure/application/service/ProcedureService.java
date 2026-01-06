package com.sopt.cherrish.domain.procedure.application.service;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

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

    private static final Collator KOREAN_COLLATOR = Collator.getInstance(Locale.KOREAN);
	private final ProcedureRepository procedureRepository;

	public ProcedureListResponseDto searchProcedures(String keyword, Long worryId) {
		List<Procedure> procedures = procedureRepository.searchProcedures(keyword, worryId);

		// DB의 한글 collation 설정과 무관하게 정확한 한글 정렬을 보장하기 위해 Java에서 정렬
		return ProcedureListResponseDto.of(
			procedures.stream()
				.map(ProcedureResponseDto::from)
                .sorted((p1, p2) -> KOREAN_COLLATOR.compare(p1.getName(), p2.getName()))
				.toList()
		);
	}
}
