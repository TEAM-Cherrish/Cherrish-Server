package com.sopt.cherrish.domain.challenge.homecare.fixture;

import java.util.Arrays;
import java.util.List;

import com.sopt.cherrish.domain.challenge.homecare.domain.model.HomecareRoutine;
import com.sopt.cherrish.domain.challenge.homecare.presentation.dto.response.HomecareRoutineResponseDto;

public class HomecareRoutineTestFixture {

	private HomecareRoutineTestFixture() {
		// Utility class
	}

	public static List<HomecareRoutineResponseDto> homecareRoutineList() {
		return Arrays.stream(HomecareRoutine.values())
			.map(HomecareRoutineResponseDto::from)
			.toList();
	}
}
