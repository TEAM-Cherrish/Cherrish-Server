package com.sopt.cherrish.domain.challenge.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.sopt.cherrish.domain.challenge.application.dto.response.HomecareRoutineResponseDto;
import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;

import lombok.RequiredArgsConstructor;

/**
 * 홈케어 루틴 조회 서비스
 */
@Service
@RequiredArgsConstructor
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
