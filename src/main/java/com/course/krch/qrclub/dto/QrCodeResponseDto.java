package com.course.krch.qrclub.dto;

import java.util.UUID;

public record QrCodeResponseDto(
        UUID id,
        UUID userId
) {
}
