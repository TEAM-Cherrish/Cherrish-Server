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

	SKIN_CONDITION(1, "피부 컨디션"),
	LIFESTYLE(2, "생활 습관"),
	BODY_CARE(3, "체형 관리"),
	WELLNESS(4, "웰니스 • 마음챙김");

	private final int id;
	private final String description;

	private static final Map<Integer, HomecareRoutine> ID_MAP =
		Arrays.stream(values())
			.collect(Collectors.toMap(HomecareRoutine::getId, routine -> routine));

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
