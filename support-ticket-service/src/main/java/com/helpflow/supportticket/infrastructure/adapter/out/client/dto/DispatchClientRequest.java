package com.helpflow.supportticket.infrastructure.adapter.out.client.dto;

public record DispatchClientRequest(
		Long ticketId,
		String categoria,
		String prioridad) {
}

