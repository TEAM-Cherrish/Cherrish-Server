package com.sopt.cherrish.domain.worry.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.worry.domain.repository.WorryRepository;
import com.sopt.cherrish.domain.worry.presentation.dto.response.WorryResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorryService {

	private final WorryRepository worryRepository;

	/**
	 * 피부 고민 목록 조회
	 *
	 * @return 피부 고민 목록
	 */
	public List<WorryResponseDto> getAllWorries() {
		return worryRepository.findAll().stream()
			.map(WorryResponseDto::from)
			.toList();
	}
}