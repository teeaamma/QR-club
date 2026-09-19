package com.course.krch.qrclub.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QrCodeRequestDto(

        @NotNull
        UUID userId
) {
}
