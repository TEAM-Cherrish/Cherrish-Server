package com.sopt.cherrish.domain.challenge.application.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.presentation.dto.response.HomecareRoutineResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 홈케어 루틴 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
