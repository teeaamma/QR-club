package com.course.krch.qrclub.dto;

import java.util.UUID;

public record ParticipantResponseDto(
        UUID id,
        String firstName,
        String lastName
) {
}
