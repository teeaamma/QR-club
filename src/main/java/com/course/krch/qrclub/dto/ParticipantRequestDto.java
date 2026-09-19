package com.course.krch.qrclub.dto;

import jakarta.validation.constraints.NotBlank;

public record ParticipantRequestDto(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName
) {
}
