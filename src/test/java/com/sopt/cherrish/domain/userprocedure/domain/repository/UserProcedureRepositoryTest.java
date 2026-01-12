package com.sopt.cherrish.domain.userprocedure.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.user.domain.model.User;
import com.sopt.cherrish.domain.userprocedure.domain.model.UserProcedure;
import com.sopt.cherrish.global.config.QueryDslConfig;

@DataJpaTest
@Import({QueryDslConfig.class, UserProcedureRepositoryTest.TestConfig.class})
@DisplayName("UserProcedureRepository 통합 테스트")
class UserProcedureRepositoryTest {

	@TestConfiguration
	@EnableJpaAuditing
	static class TestConfig {
	}

	@Autowired
	private UserProcedureRepository userProcedureRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	@DisplayName("UserProcedure 저장 및 조회 성공")
	void saveAndFindUserProcedure() {
		// given
		User user = createAndPersistUser("홍길동", 25);
		Procedure procedure = createAndPersistProcedure("레이저 토닝", "레이저", 0, 1);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 1, 15, 14, 30);

		UserProcedure userProcedure = UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(5)
			.build();

		// when
		UserProcedure saved = userProcedureRepository.save(userProcedure);
		entityManager.flush();
		entityManager.clear();

		// then
		Optional<UserProcedure> found = userProcedureRepository.findById(saved.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
		assertThat(found.get().getProcedure().getId()).isEqualTo(procedure.getId());
		assertThat(found.get().getScheduledAt()).isEqualTo(scheduledAt);
		assertThat(found.get().getDowntimeDays()).isEqualTo(5);
	}

	@Test
	@DisplayName("가장 최근 날짜의 시술 목록 조회 성공")
	void findProceduresOnMostRecentDate() {
		// given
		User user = createAndPersistUser("홍길동", 25);
		Procedure procedure = createAndPersistProcedure("레이저 토닝", "레이저", 0, 1);

		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 14, 10, 0))
			.downtimeDays(3)
			.build());
		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 15, 9, 0))
			.downtimeDays(5)
			.build());
		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 15, 18, 0))
			.downtimeDays(1)
			.build());

		entityManager.flush();
		entityManager.clear();

		// when
		List<UserProcedure> result = userProcedureRepository.findProceduresOnMostRecentDate(
			user.getId(), LocalDate.of(2026, 1, 15)
		);

		// then
		assertThat(result).hasSize(2);
		assertThat(result)
			.allMatch(up -> up.getScheduledAt().toLocalDate().equals(LocalDate.of(2026, 1, 15)));
	}

	@Test
	@DisplayName("가장 최근 날짜 조회 시 시술이 없으면 빈 리스트 반환")
	void findProceduresOnMostRecentDateEmpty() {
		// given
		User user = createAndPersistUser("홍길동", 25);

		// when
		List<UserProcedure> result = userProcedureRepository.findProceduresOnMostRecentDate(
			user.getId(), LocalDate.of(2026, 1, 15)
		);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("다가오는 시술 조회 - 기준일 포함 및 시간순 정렬")
	void findUpcomingProceduresGroupedByDate() {
		// given
		User user = createAndPersistUser("홍길동", 25);
		Procedure procedure = createAndPersistProcedure("레이저 토닝", "레이저", 0, 1);

		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 15, 9, 0))
			.downtimeDays(1)
			.build());
		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 16, 9, 0))
			.downtimeDays(2)
			.build());
		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 16, 13, 0))
			.downtimeDays(3)
			.build());
		userProcedureRepository.save(UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(LocalDateTime.of(2026, 1, 17, 9, 0))
			.downtimeDays(4)
			.build());

		entityManager.flush();
		entityManager.clear();

		// when
		List<UserProcedure> result = userProcedureRepository.findUpcomingProceduresGroupedByDate(
			user.getId(), LocalDate.of(2026, 1, 16)
		);

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getScheduledAt()).isEqualTo(LocalDateTime.of(2026, 1, 16, 9, 0));
		assertThat(result.get(2).getScheduledAt()).isEqualTo(LocalDateTime.of(2026, 1, 17, 9, 0));
	}

	// Helper methods
	private User createAndPersistUser(String name, int age) {
		User user = User.builder()
			.name(name)
			.age(age)
			.build();
		return entityManager.persist(user);
	}

	private Procedure createAndPersistProcedure(String name, String category, int minDowntimeDays, int maxDowntimeDays) {
		Procedure procedure = Procedure.builder()
			.name(name)
			.category(category)
			.minDowntimeDays(minDowntimeDays)
			.maxDowntimeDays(maxDowntimeDays)
			.build();
		return entityManager.persist(procedure);
	}
}
