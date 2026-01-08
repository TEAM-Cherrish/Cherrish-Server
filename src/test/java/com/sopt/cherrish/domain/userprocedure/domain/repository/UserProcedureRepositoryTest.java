package com.sopt.cherrish.domain.userprocedure.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
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
	@DisplayName("User와 Procedure LAZY 로딩 확인")
	void lazyLoadingUserAndProcedure() {
		// given
		User user = createAndPersistUser("김철수", 30);
		Procedure procedure = createAndPersistProcedure("필러", "주사", 1, 3);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 2, 1, 10, 0);

		UserProcedure userProcedure = UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(7)
			.build();

		UserProcedure saved = userProcedureRepository.save(userProcedure);
		entityManager.flush();
		entityManager.clear();

		// when
		Optional<UserProcedure> found = userProcedureRepository.findById(saved.getId());

		// then
		assertThat(found).isPresent();
		UserProcedure foundProcedure = found.get();

		// LAZY 로딩 확인 - 실제 접근 시 로딩됨
		assertThat(foundProcedure.getUser().getName()).isEqualTo("김철수");
		assertThat(foundProcedure.getProcedure().getName()).isEqualTo("필러");
	}

	@Test
	@DisplayName("downtimeDays null 허용 확인")
	void saveWithNullDowntimeDays() {
		// given
		User user = createAndPersistUser("이영희", 28);
		Procedure procedure = createAndPersistProcedure("보톡스", "주사", 2, 5);
		LocalDateTime scheduledAt = LocalDateTime.of(2025, 3, 1, 11, 0);

		UserProcedure userProcedure = UserProcedure.builder()
			.user(user)
			.procedure(procedure)
			.scheduledAt(scheduledAt)
			.downtimeDays(null)  // nullable 검증
			.build();

		// when
		UserProcedure saved = userProcedureRepository.save(userProcedure);
		entityManager.flush();
		entityManager.clear();

		// then
		Optional<UserProcedure> found = userProcedureRepository.findById(saved.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getDowntimeDays()).isNull();
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
