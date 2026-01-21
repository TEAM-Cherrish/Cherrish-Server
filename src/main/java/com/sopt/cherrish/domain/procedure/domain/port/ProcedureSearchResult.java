package com.sopt.cherrish.domain.procedure.domain.port;

import java.util.List;

public record ProcedureSearchResult(Status status, List<Long> procedureIds) {

	public enum Status {
		AVAILABLE,
		UNAVAILABLE
	}

	public static ProcedureSearchResult available(List<Long> procedureIds) {
		return new ProcedureSearchResult(Status.AVAILABLE, procedureIds != null ? procedureIds : List.of());
	}

	public static ProcedureSearchResult unavailable() {
		return new ProcedureSearchResult(Status.UNAVAILABLE, List.of());
	}

	public boolean isAvailable() {
		return status == Status.AVAILABLE;
	}

	public boolean isUnavailable() {
		return status == Status.UNAVAILABLE;
	}
}
