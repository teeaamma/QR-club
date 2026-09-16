package com.course.krch.qrclub.mapper;

import com.course.krch.qrclub.dto.QrCodeResponseDto;
import com.course.krch.qrclub.entity.QrCode;

public class QrCodeMapper {

    public static QrCodeResponseDto toDto(QrCode code){
        return new QrCodeResponseDto(
                code.getId(),
                code.getParticipant().getId()
        );
    }
}
