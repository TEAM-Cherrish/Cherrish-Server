package com.sopt.cherrish.domain.worry.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sopt.cherrish.domain.worry.domain.model.Worry;
import com.sopt.cherrish.domain.worry.domain.repository.WorryRepository;
import com.sopt.cherrish.domain.worry.fixture.WorryFixture;
import com.sopt.cherrish.domain.worry.presentation.dto.response.WorryResponseDto;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorryService 단위 테스트")
class WorryServiceTest {

	@InjectMocks
	private WorryService worryService;

	@Mock
	private WorryRepository worryRepository;

	@Test
	@DisplayName("피부 고민 전체 조회 성공")
	void getAllWorriesSuccess() {
		// given
		List<Worry> worries = Arrays.asList(
			WorryFixture.createWorry(1L, "여드름/트러블"),
			WorryFixture.createWorry(2L, "색소/잡티"),
			WorryFixture.createWorry(3L, "홍조"),
			WorryFixture.createWorry(4L, "탄력/주름"),
			WorryFixture.createWorry(5L, "모공"),
			WorryFixture.createWorry(6L, "피부결/각질")
		);

		given(worryRepository.findAll()).willReturn(worries);

		// when
		List<WorryResponseDto> result = worryService.getAllWorries();

		// then
		assertThat(result).isNotNull();
		assertThat(result).hasSize(6);
		assertThat(result.get(0).getContent()).isEqualTo("여드름/트러블");
		assertThat(result.get(1).getContent()).isEqualTo("색소/잡티");
		assertThat(result.get(2).getContent()).isEqualTo("홍조");
		assertThat(result.get(3).getContent()).isEqualTo("탄력/주름");
		assertThat(result.get(4).getContent()).isEqualTo("모공");
		assertThat(result.get(5).getContent()).isEqualTo("피부결/각질");
	}

	@Test
	@DisplayName("피부 고민 전체 조회 - 빈 리스트 반환")
	void getAllWorriesEmpty() {
		// given
		given(worryRepository.findAll()).willReturn(List.of());

		// when
		List<WorryResponseDto> result = worryService.getAllWorries();

		// then
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}
}
