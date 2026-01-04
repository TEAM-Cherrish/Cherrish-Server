package com.sopt.cherrish.domain.procedure.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.procedure.application.service.ProcedureService;
import com.sopt.cherrish.domain.procedure.exception.ProcedureErrorCode;
import com.sopt.cherrish.domain.procedure.presentation.dto.request.ProcedureSearchRequestDto;
import com.sopt.cherrish.domain.procedure.presentation.dto.response.ProcedureListResponseDto;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/procedures")
@RequiredArgsConstructor
@Tag(name = "Procedure", description = "시술 관련 API")
public class ProcedureController {

	private final ProcedureService procedureService;

	@Operation(
		summary = "시술 목록 조회",
		description = "검색 키워드 또는 피부 고민으로 시술 목록을 조회합니다. 파라미터를 제공하지 않으면 전체 시술 목록을 반환합니다."
	)
	@ApiExceptions({ProcedureErrorCode.class, ErrorCode.class})
	@GetMapping
	public CommonApiResponse<ProcedureListResponseDto> getProcedures(
		@Valid @ModelAttribute ProcedureSearchRequestDto request
	) {
		ProcedureListResponseDto response = procedureService.searchProcedures(
			request.getKeyword(),
			request.getWorryId()
		);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
