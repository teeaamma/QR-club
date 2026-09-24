package com.course.krch.qrclub.mapper;

import com.course.krch.qrclub.dto.ParticipantRequestDto;
import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.entity.Participant;

public class ParticipantMapper {

    public static ParticipantResponseDto toDto(Participant p){
        return new ParticipantResponseDto(
                p.getId(),
                p.getFirstName(),
                p.getLastName()
        );
    }

    public static Participant toEntity(ParticipantRequestDto dto){
        return new Participant(
                dto.firstName(),
                dto.lastName()
        );
    }
}
