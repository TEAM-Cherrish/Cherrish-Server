package com.sopt.cherrish.domain.procedure.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.sopt.cherrish.domain.procedure.domain.model.Procedure;
import com.sopt.cherrish.domain.procedure.domain.model.ProcedureWorry;
import com.sopt.cherrish.domain.worry.domain.model.Worry;
import com.sopt.cherrish.domain.worry.domain.repository.WorryRepository;
import com.sopt.cherrish.global.config.QueryDslConfig;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QueryDslConfig.class)
@EnableJpaAuditing
@DisplayName("ProcedureRepository 통합 테스트")
class ProcedureRepositoryTest {

	@Autowired
	private ProcedureRepository procedureRepository;

	@Autowired
	private WorryRepository worryRepository;

	@Autowired
	private ProcedureWorryRepository procedureWorryRepository;

	private Procedure laserToning;
	private Procedure fraxelLaser;
	private Procedure botox;
	private Worry worry1;
	private Worry worry2;

	@BeforeEach
	void setUp() {
		// Given: 테스트 데이터 생성
		worry1 = Worry.builder()
			.content("여드름/트러블")
			.build();
		worry2 = Worry.builder()
			.content("색소/잡티")
			.build();
		worryRepository.save(worry1);
		worryRepository.save(worry2);

		laserToning = Procedure.builder()
			.name("레이저 토닝")
			.category("레이저")
			.minDowntimeDays(3)
			.maxDowntimeDays(7)
			.build();

		fraxelLaser = Procedure.builder()
			.name("프락셀 레이저")
			.category("레이저")
			.minDowntimeDays(5)
			.maxDowntimeDays(10)
			.build();

		botox = Procedure.builder()
			.name("보톡스")
			.category("보톡스/필러")
			.minDowntimeDays(1)
			.maxDowntimeDays(3)
			.build();

		procedureRepository.save(laserToning);
		procedureRepository.save(fraxelLaser);
		procedureRepository.save(botox);

		// 시술-피부고민 연결
		ProcedureWorry pw1 = ProcedureWorry.builder()
			.procedure(laserToning)
			.worry(worry2)
			.build();
		ProcedureWorry pw2 = ProcedureWorry.builder()
			.procedure(fraxelLaser)
			.worry(worry1)
			.build();
		ProcedureWorry pw3 = ProcedureWorry.builder()
			.procedure(fraxelLaser)
			.worry(worry2)
			.build();

		procedureWorryRepository.save(pw1);
		procedureWorryRepository.save(pw2);
		procedureWorryRepository.save(pw3);
	}

	@Test
	@DisplayName("키워드로 시술 검색 성공")
	void searchProceduresByKeyword() {
		// When: "레이저" 키워드로 검색
		List<Procedure> result = procedureRepository.searchProcedures("레이저", null);

		// Then: 레이저가 포함된 시술만 반환
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Procedure::getName)
			.containsExactlyInAnyOrder("레이저 토닝", "프락셀 레이저");
	}

	@Test
	@DisplayName("피부 고민 ID로 시술 필터링 성공")
	void searchProceduresByWorryId() {
		// When: worry2에 해당하는 시술 검색
		List<Procedure> result = procedureRepository.searchProcedures(null, worry2.getId());

		// Then: worry2와 연결된 시술만 반환
		assertThat(result).hasSize(2);
		assertThat(result).extracting(Procedure::getName)
			.containsExactlyInAnyOrder("레이저 토닝", "프락셀 레이저");
	}

	@Test
	@DisplayName("키워드와 피부 고민 ID로 동시 검색 성공")
	void searchProceduresByKeywordAndWorryId() {
		// When: "레이저" 키워드 + worry1 조건으로 검색
		List<Procedure> result = procedureRepository.searchProcedures("레이저", worry1.getId());

		// Then: 두 조건을 모두 만족하는 시술만 반환
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("프락셀 레이저");
	}

	@Test
	@DisplayName("검색 조건 없이 전체 시술 조회")
	void searchAllProcedures() {
		// When: 조건 없이 검색
		List<Procedure> result = procedureRepository.searchProcedures(null, null);

		// Then: 모든 시술 반환
		assertThat(result).hasSize(3);
		assertThat(result).extracting(Procedure::getName)
			.containsExactlyInAnyOrder("레이저 토닝", "프락셀 레이저", "보톡스");
	}

	@Test
	@DisplayName("검색 결과가 없는 경우")
	void searchProceduresWithNoResults() {
		// When: 존재하지 않는 키워드로 검색
		List<Procedure> result = procedureRepository.searchProcedures("존재하지않는시술", null);

		// Then: 빈 리스트 반환
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("존재하지 않는 피부 고민 ID로 검색")
	void searchProceduresByNonExistentWorryId() {
		// When: 존재하지 않는 worryId로 검색
		List<Procedure> result = procedureRepository.searchProcedures(null, 999L);

		// Then: 빈 리스트 반환
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("빈 ID 목록으로 시술 조회")
	void findByIdInWithEmptyIds() {
		// When: 빈 ID 목록으로 조회
		List<Procedure> result = procedureRepository.findByIdInAndWorryId(List.of(), null);

		// Then: 빈 리스트 반환
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("null ID 목록으로 시술 조회")
	void findByIdInWithNullIds() {
		// When: null ID 목록으로 조회
		List<Procedure> result = procedureRepository.findByIdInAndWorryId(null, null);

		// Then: 빈 리스트 반환
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("ID 목록과 피부 고민 ID로 시술 조회 성공")
	void findByIdInAndWorryId() {
		// When: 레이저 토닝, 보톡스 ID로 worry2 조건 조회
		List<Long> ids = List.of(laserToning.getId(), botox.getId());
		List<Procedure> result = procedureRepository.findByIdInAndWorryId(ids, worry2.getId());

		// Then: worry2와 연결된 레이저 토닝만 반환
		assertThat(result).extracting(Procedure::getName)
			.containsExactlyInAnyOrder("레이저 토닝");
	}

	@Test
	@DisplayName("ID 목록으로 시술 조회 성공")
	void findByIdInWithoutWorryFilter() {
		// When: 레이저 토닝, 보톡스 ID로 조회
		List<Long> ids = List.of(laserToning.getId(), botox.getId());
		List<Procedure> result = procedureRepository.findByIdInAndWorryId(ids, null);

		// Then: ID 목록에 해당하는 시술 반환
		assertThat(result).extracting(Procedure::getName)
			.containsExactlyInAnyOrder("레이저 토닝", "보톡스");
	}
}
