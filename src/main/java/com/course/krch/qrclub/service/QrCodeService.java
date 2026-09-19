package com.course.krch.qrclub.service;

import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.dto.QrCodeRequestDto;
import com.course.krch.qrclub.dto.QrCodeResponseDto;
import com.course.krch.qrclub.entity.Participant;
import com.course.krch.qrclub.entity.QrCode;
import com.course.krch.qrclub.exception.NotFoundException;
import com.course.krch.qrclub.mapper.ParticipantMapper;
import com.course.krch.qrclub.mapper.QrCodeMapper;
import com.course.krch.qrclub.repository.QrCodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class QrCodeService {

    private final QrCodeRepository repository;
    private final ParticipantService participantService;

    public QrCodeService(QrCodeRepository repository, ParticipantService participantService) {
        this.repository = repository;
        this.participantService = participantService;
    }

    @Transactional(readOnly = true)
    public Page<QrCodeResponseDto> getAll(Pageable pageable){
        return repository.findAllByIsDeletedFalse(pageable)
                .map(QrCodeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public QrCodeResponseDto getById(UUID id){
        QrCode qrCode = repository.findByIdAndIsDeletedFalseOrThrow(id);
        return QrCodeMapper.toDto(qrCode);
    }

    public QrCodeResponseDto create(QrCodeRequestDto dto){
        Participant participant = participantService.getParticipantById(dto.userId());

        QrCode code = repository.save(new QrCode(participant));
        return QrCodeMapper.toDto(code);
    }

    @Transactional
    public QrCodeResponseDto update(UUID id, QrCodeRequestDto dto){
        QrCode qrCode = repository.findByIdAndIsDeletedFalseOrThrow(id);

        Participant newParticipant = participantService.getParticipantById(dto.userId());
        qrCode.setParticipant(newParticipant);

        QrCode updatedQrCode = repository.save(qrCode);
        return QrCodeMapper.toDto(updatedQrCode);
    }

    @Transactional
    public void delete(UUID id){
        int updatedRows = repository.softDeleteById(id);

        if (updatedRows == 0)
            throw new NotFoundException("QR-кода с id = " + id + " не существует");
    }

    @Transactional
    public ParticipantResponseDto scan(UUID id){
        QrCode qrCode = repository.findByIdAndIsDeletedFalseOrThrow(id);

        Participant participant = qrCode.getParticipant();
        this.delete(qrCode.getId());
        this.create(new QrCodeRequestDto(participant.getId()));

        return ParticipantMapper.toDto(participant);
    }
}
