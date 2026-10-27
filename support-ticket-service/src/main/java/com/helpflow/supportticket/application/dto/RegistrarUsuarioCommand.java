package com.helpflow.supportticket.application.dto;

public record RegistrarUsuarioCommand(
		String documento,
		String nombre,
		String correoCorporativo,
		String sede,
		String area,
		String password,
		Actor actor) {
}
