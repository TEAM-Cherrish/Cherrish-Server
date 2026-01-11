package com.sopt.cherrish.domain.challenge.core.application.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.repository.ChallengeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeSchedulerService {

	private final ChallengeRepository challengeRepository;

	/**
	 * 매일 00시(Asia/Seoul)에 만료된 챌린지를 비활성화합니다.
	 * 벌크 업데이트를 사용하여 단일 UPDATE 쿼리로 모든 만료된 챌린지를 한 번에 처리합니다.
	 */
	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	@Transactional
	public void expireCompletedChallenges() {
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		log.info("챌린지 만료 스케줄러 시작: {}", today);

		int updatedCount = challengeRepository.bulkUpdateExpiredChallenges(today);

		log.info("챌린지 만료 스케줄러 완료: {}개 처리", updatedCount);
	}
}
