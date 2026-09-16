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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QrCodeService {

    private final QrCodeRepository repository;
    private final ParticipantService participantService;

    public QrCodeService(QrCodeRepository repository, ParticipantService participantService) {
        this.repository = repository;
        this.participantService = participantService;
    }

    public List<QrCodeResponseDto> getAll(){
        return repository.findAll()
                .stream()
                .map(QrCodeMapper::toDto)
                .toList();
    }

    public QrCodeResponseDto getById(UUID id){
        QrCode qrCode = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("QR-кода с id = " + id + " не существует")
                );
        return QrCodeMapper.toDto(qrCode);
    }

    public QrCodeResponseDto create(QrCodeRequestDto dto){
        Participant participant = participantService.getParticipantById(dto.userId());

        QrCode code = repository.save(new QrCode(participant));
        return QrCodeMapper.toDto(code);
    }

    public QrCodeResponseDto update(UUID id, QrCodeRequestDto dto){
        QrCode qrCode = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("QR-кода с id = " + id + " не существует")
                );

        Participant newParticipant = participantService.getParticipantById(dto.userId());
        qrCode.setParticipant(newParticipant);

        QrCode updatedQrCode = repository.save(qrCode);
        return QrCodeMapper.toDto(updatedQrCode);
    }

    public void delete(UUID id){
        if (!repository.existsById(id))
            throw new NotFoundException("QR-кода с id = " + id + " не существует");

        repository.deleteById(id);
    }

    public ParticipantResponseDto scan(UUID id){
        QrCode qrCode = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("QR-кода с id = " + id + " не существует")
                );

        Participant participant = qrCode.getParticipant();
        this.delete(qrCode.getId());
        this.create(new QrCodeRequestDto(participant.getId()));

        return ParticipantMapper.toDto(participant);
    }
}
