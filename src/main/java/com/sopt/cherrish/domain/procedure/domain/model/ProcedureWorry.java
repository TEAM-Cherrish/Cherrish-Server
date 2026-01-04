package com.sopt.cherrish.domain.procedure.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.sopt.cherrish.domain.worry.domain.model.Worry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "procedure_worries")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ProcedureWorry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "procedure_id", nullable = false)
	private Procedure procedure;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "worry_id", nullable = false)
	private Worry worry;

	@CreatedDate
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	private ProcedureWorry(Procedure procedure, Worry worry) {
		this.procedure = procedure;
		this.worry = worry;
	}

	public Long getId() {
		return id;
	}

	public Procedure getProcedure() {
		return procedure;
	}

	public Worry getWorry() {
		return worry;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
