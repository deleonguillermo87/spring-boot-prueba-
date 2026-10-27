package com.helpflow.supportticket.application.dto;

import java.time.Duration;

public record EscalarTicketsCommand(Duration umbralSla, Actor actor) {
}
