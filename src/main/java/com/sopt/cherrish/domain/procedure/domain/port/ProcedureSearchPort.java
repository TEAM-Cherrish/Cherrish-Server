package com.sopt.cherrish.domain.procedure.domain.port;

import java.util.List;

public interface ProcedureSearchPort {

	boolean isAvailable();

	List<Long> searchByKeyword(String keyword);
}
