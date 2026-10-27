package com.helpflow.supportticket.application.dto;

public record LoginCommand(String correoCorporativo, String password, Actor actor) {
}
