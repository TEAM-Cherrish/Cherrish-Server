package com.sopt.cherrish.domain.challenge.homecare.presentation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.challenge.homecare.application.service.HomecareRoutineService;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.success.SuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Tag(name = "Homecare Routine", description = "홈케어 루틴 관련 API")
public class HomecareRoutineController {

	private final HomecareRoutineService homecareRoutineService;

	@Operation(
		summary = "홈케어 루틴 목록 조회",
		description = "사용 가능한 모든 홈케어 루틴 카테고리 목록을 조회합니다."
	)
	@GetMapping("/homecare-routines")
	public CommonApiResponse<List<HomecareRoutineResponseDto>> getHomecareRoutines() {
		List<HomecareRoutineResponseDto> routines = homecareRoutineService.getAllHomecareRoutines();
		return CommonApiResponse.success(SuccessCode.SUCCESS, routines);
	}
}
