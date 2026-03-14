package com.authservice.app.domain.sample.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;
import com.authservice.app.domain.sample.entity.Sample;

public interface SampleRepository extends JpaRepository<Sample, Long> {
	@Query("SELECT s FROM Sample s WHERE s.id = :id")
	Optional<Sample> findSampleByIdAndVersion(@Param("id") Long id);
}