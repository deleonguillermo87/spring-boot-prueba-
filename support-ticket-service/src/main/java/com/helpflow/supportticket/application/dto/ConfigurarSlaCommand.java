package com.helpflow.supportticket.application.dto;

import java.time.Duration;
import java.util.Map;

import com.helpflow.supportticket.domain.model.Prioridad;

public record ConfigurarSlaCommand(Map<Prioridad, Duration> plazos, Actor actor) {
}
