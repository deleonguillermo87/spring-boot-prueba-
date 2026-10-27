package com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.AsignacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignacionJpaRepository extends JpaRepository<AsignacionEntity, Long> {
}
