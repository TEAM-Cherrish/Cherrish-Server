package com.sopt.cherrish.domain.challenge.core.application.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sopt.cherrish.domain.challenge.core.domain.model.Challenge;
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
	 * endDate가 현재 날짜보다 이전인 활성 챌린지를 조회하여 complete() 처리합니다.
	 */
	@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
	@Transactional
	public void expireCompletedChallenges() {
		LocalDate today = LocalDate.now();
		log.info("챌린지 만료 스케줄러 시작: {}", today);

		List<Challenge> expiredChallenges = challengeRepository
			.findByIsActiveTrueAndEndDateBefore(today);

		log.info("만료 대상 챌린지 {}개 발견", expiredChallenges.size());

		expiredChallenges.forEach(Challenge::complete);

		log.info("챌린지 만료 스케줄러 완료: {}개 처리", expiredChallenges.size());
	}
}
