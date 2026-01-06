package com.sopt.cherrish.domain.challenge.homecare.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;

/**
 * 홈케어 루틴 조회 서비스
 */
@Service
public class HomecareRoutineService {

	/**
	 * 모든 홈케어 루틴 목록 조회
	 *
	 * @return 홈케어 루틴 목록
	 */
	public List<HomecareRoutineResponseDto> getAllHomecareRoutines() {

		return Arrays.stream(HomecareRoutine.values())
			.map(HomecareRoutineResponseDto::from)
			.toList();
	}
}
