package com.helpflow.supportticket.infrastructure.adapter.out.persistence.repository;

import com.helpflow.supportticket.infrastructure.adapter.out.persistence.entity.SlaConfigEntity;
import com.helpflow.supportticket.domain.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaConfigJpaRepository extends JpaRepository<SlaConfigEntity, Prioridad> {
}
