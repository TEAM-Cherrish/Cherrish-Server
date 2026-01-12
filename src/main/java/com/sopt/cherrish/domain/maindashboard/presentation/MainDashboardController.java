package com.sopt.cherrish.domain.maindashboard.presentation;

import com.sopt.cherrish.global.annotation.ApiExceptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sopt.cherrish.domain.maindashboard.application.facade.MainDashboardFacade;
import com.sopt.cherrish.domain.maindashboard.presentation.dto.response.MainDashboardResponseDto;
import com.sopt.cherrish.global.response.CommonApiResponse;
import com.sopt.cherrish.global.response.error.ErrorCode;
import com.sopt.cherrish.global.response.success.SuccessCode;
import com.sopt.cherrish.domain.user.exception.UserErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/main-dashboard")
@RequiredArgsConstructor
@Tag(name = "Main Dashboard", description = "메인 대시보드 API")
public class MainDashboardController {

	private final MainDashboardFacade mainDashboardFacade;

	@Operation(
		summary = "메인 대시보드 조회",
		description = "사용자의 챌린지 진행률, 최근 시술, 예정된 시술 정보를 조회합니다."
	)
	@ApiExceptions({UserErrorCode.class, ErrorCode.class})
	@GetMapping
	public CommonApiResponse<MainDashboardResponseDto> getMainDashboard(
		@Parameter(description = "사용자 ID", required = true, example = "1")
		@RequestHeader("X-User-Id") Long userId
	) {
		MainDashboardResponseDto response = mainDashboardFacade.getMainDashboard(userId);
		return CommonApiResponse.success(SuccessCode.SUCCESS, response);
	}
}
