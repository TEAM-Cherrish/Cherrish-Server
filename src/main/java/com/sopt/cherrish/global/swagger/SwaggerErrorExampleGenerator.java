package com.sopt.cherrish.global.swagger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sopt.cherrish.global.response.error.ErrorCode;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class SwaggerErrorExampleGenerator {

	private static final String MEDIA_TYPE_JSON = "application/json";

	public void addErrorResponse(Operation operation, ErrorCode[] errorCodes) {
		ApiResponses responses = operation.getResponses();
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders = groupByStatus(errorCodes);
		addExamplesToResponses(responses, statusWithExampleHolders);
	}

	private Map<Integer, List<ExampleHolder>> groupByStatus(ErrorCode[] errorCodes) {
		return Arrays.stream(errorCodes)
			.map(this::createExampleHolder)
			.collect(Collectors.groupingBy(ExampleHolder::code));
	}

	private ExampleHolder createExampleHolder(ErrorCode errorCode) {
		return ExampleHolder.of(
			createSwaggerExample(errorCode),
			errorCode.name(),
			errorCode.getStatus()
		);
	}

	private Example createSwaggerExample(ErrorCode errorCode) {
		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("code", errorCode.getCode());
		errorResponse.put("message", errorCode.getMessage());
		errorResponse.put("data", null);

		Example example = new Example();
		example.setValue(errorResponse);
		return example;
	}

	private void addExamplesToResponses(ApiResponses responses,
		Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
		statusWithExampleHolders.forEach((status, exampleHolders) -> {
			ApiResponse apiResponse = createApiResponseWithExamples(exampleHolders);
			responses.addApiResponse(String.valueOf(status), apiResponse);
		});
	}

	private ApiResponse createApiResponseWithExamples(List<ExampleHolder> exampleHolders) {
		MediaType mediaType = new MediaType();
		exampleHolders.forEach(holder ->
			mediaType.addExamples(holder.name(), holder.holder())
		);

		Content content = new Content();
		content.addMediaType(MEDIA_TYPE_JSON, mediaType);

		ApiResponse apiResponse = new ApiResponse();
		apiResponse.setContent(content);
		return apiResponse;
	}

}
