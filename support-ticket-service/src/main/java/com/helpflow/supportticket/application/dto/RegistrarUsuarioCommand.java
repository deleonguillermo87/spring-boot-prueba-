package com.helpflow.supportticket.application.dto;

public record RegistrarUsuarioCommand(
		String documento,
		String nombre,
		String correoCorporativo,
		String sede,
		String area,
		String password,
		Actor actor,
		String rolSolicitado,
		String codigoTecnico) {

	public RegistrarUsuarioCommand(
			String documento,
			String nombre,
			String correoCorporativo,
			String sede,
			String area,
			String password,
			Actor actor) {
		this(documento, nombre, correoCorporativo, sede, area, password, actor, null, null);
	}
}
