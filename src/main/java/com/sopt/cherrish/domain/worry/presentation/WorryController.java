package com.sopt.cherrish.domain.worry.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.worry.application.service.WorryService;
import com.sopt.cherrish.domain.worry.presentation.dto.response.WorryResponseDto;
import com.sopt.cherrish.global.annotation.ApiExceptions;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/worries")
@RequiredArgsConstructor
@Tag(name = "Worry", description = "피부 고민 관련 API")
public class WorryController {

	private final WorryService worryService;

	@Operation(
		summary = "피부 고민 목록 조회",
		description = "모든 피부 고민 목록을 조회합니다."
	)
	@ApiExceptions({ErrorCode.class})
	@GetMapping
	public CommonApiResponse<List<WorryResponseDto>> getAllWorries() {
		List<WorryResponseDto> response = worryService.getAllWorries();
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
