package com.sopt.cherrish.domain.challenge.homecare.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 홈케어 루틴 카테고리 enum
 */
@Getter
@RequiredArgsConstructor
public enum HomecareRoutine {

	SKIN_MOISTURIZING(1, "피부 보습 관리"),
	SKIN_BRIGHTENING(2, "피부 미백 관리"),
	WRINKLE_CARE(3, "주름 개선 관리"),
	TROUBLE_CARE(4, "트러블 케어"),
	PORE_CARE(5, "모공 관리"),
	ELASTICITY_CARE(6, "탄력 관리");

	private static final Map<Integer, HomecareRoutine> ID_MAP =
		Arrays.stream(values())
			.collect(Collectors.toMap(HomecareRoutine::getId, routine -> routine));
	private final int id;
	private final String description;

	/**
	 * ID로 HomecareRoutine 찾기
	 *
	 * @param id 홈케어 루틴 ID (1~6)
	 * @return 해당하는 HomecareRoutine enum
	 * @throws ChallengeException 존재하지 않는 ID인 경우
	 */
	public static HomecareRoutine fromId(int id) {
		HomecareRoutine routine = ID_MAP.get(id);
		if (routine == null) {
			throw new ChallengeException(ChallengeErrorCode.INVALID_HOMECARE_ROUTINE_ID);
		}
		return routine;
	}
}
