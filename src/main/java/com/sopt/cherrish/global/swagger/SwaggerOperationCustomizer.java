package com.sopt.cherrish.global.swagger;

import java.lang.reflect.Method;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import com.sopt.cherrish.global.annotation.ApiExceptions;

import io.swagger.v3.oas.models.Operation;

public class SwaggerOperationCustomizer implements OperationCustomizer {

	private final SwaggerErrorExampleGenerator errorExampleGenerator;

	public SwaggerOperationCustomizer(SwaggerErrorExampleGenerator errorExampleGenerator) {
		this.errorExampleGenerator = errorExampleGenerator;
	}

	@Override
	public Operation customize(Operation operation, HandlerMethod handlerMethod) {
		ApiExceptions apiExceptions = resolveApiExceptions(handlerMethod);

		if (apiExceptions != null) {
			errorExampleGenerator.addErrorResponse(operation, apiExceptions.value());
		}

		return operation;
	}

	private ApiExceptions resolveApiExceptions(HandlerMethod handlerMethod) {
		ApiExceptions ann = handlerMethod.getMethodAnnotation(ApiExceptions.class);
		if (ann != null) {
			return ann;
		}

		Method method = handlerMethod.getMethod();
		Class<?> beanType = handlerMethod.getBeanType();
		for (Class<?> i : beanType.getInterfaces()) {
			try {
				Method interfaceMethod = i.getMethod(method.getName(), method.getParameterTypes());
				ApiExceptions fromInterface = interfaceMethod.getAnnotation(ApiExceptions.class);
				if (fromInterface != null) {
					return fromInterface;
				}
			} catch (NoSuchMethodException ignored) {
			}
		}
		return null;
	}

}
