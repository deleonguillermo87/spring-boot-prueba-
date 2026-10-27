package com.helpflow.supportticket.application.dto;

import com.helpflow.supportticket.domain.model.Rol;

public record LoginResult(Long usuarioId, Rol rol, String token) {
}
