package com.helpflow.supportticket.infrastructure.adapter.in.rest.dto;

import java.util.Map;

import com.helpflow.supportticket.domain.model.Prioridad;

public record SlaResponse(Map<Prioridad, Long> plazos) {
}

