package com.sopt.cherrish.domain.challenge.core.domain.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.sopt.cherrish.domain.challenge.core.exception.ChallengeErrorCode;
import com.sopt.cherrish.domain.challenge.core.exception.ChallengeException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 체리 레벨 enum
 * 진행률에 따라 체리가 성장하는 단계를 표현
 */
@Getter
@RequiredArgsConstructor
public enum CherryLevel {

	LEVEL_1(1, "몽롱체리"),    // 0-24%
	LEVEL_2(2, "뽀득체리"),    // 25-49%
	LEVEL_3(3, "팡팡체리"),    // 50-74%
	LEVEL_4(4, "꾸꾸체리");    // 75-100%

	private final int level;
	private final String name;

	private static final Map<Integer, CherryLevel> LEVEL_MAP =
		Arrays.stream(values())
			.collect(Collectors.toMap(CherryLevel::getLevel, cherryLevel -> cherryLevel));

	/**
	 * 레벨 숫자로 CherryLevel 찾기
	 *
	 * @param level 체리 레벨 (1-4)
	 * @return 해당하는 CherryLevel enum
	 * @throws ChallengeException 존재하지 않는 레벨인 경우
	 */
	public static CherryLevel fromLevel(int level) {
		CherryLevel cherryLevel = LEVEL_MAP.get(level);
		if (cherryLevel == null) {
			throw new ChallengeException(ChallengeErrorCode.INVALID_CHERRY_LEVEL);
		}
		return cherryLevel;
	}
}
