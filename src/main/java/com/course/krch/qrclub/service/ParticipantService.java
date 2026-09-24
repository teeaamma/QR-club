package com.course.krch.qrclub.service;

import com.course.krch.qrclub.dto.ParticipantRequestDto;
import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.entity.Participant;
import com.course.krch.qrclub.exception.NotFoundException;
import com.course.krch.qrclub.mapper.ParticipantMapper;
import com.course.krch.qrclub.repository.ParticipantRepository;
import com.course.krch.qrclub.repository.QrCodeRepository;
import com.course.krch.qrclub.specification.ParticipantSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final QrCodeRepository qrCodeRepository;

    public ParticipantService(ParticipantRepository participantRepository, QrCodeRepository qrCodeRepository) {
        this.participantRepository = participantRepository;
        this.qrCodeRepository = qrCodeRepository;
    }

    @Transactional(readOnly = true)
    public Page<ParticipantResponseDto> getAll(String firstName, String lastName, Pageable pageable){

        Specification<Participant> specification = Specification.allOf(
                ParticipantSpecification.firstNameContains(firstName),
                ParticipantSpecification.lastNameContains(lastName),
                ParticipantSpecification.notDeleted()
        );

        return participantRepository.findAll(specification, pageable)
                .map(ParticipantMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ParticipantResponseDto getById(UUID id){
        Participant participant = participantRepository.findByIdAndIsDeletedFalseOrThrow(id);
        return ParticipantMapper.toDto(participant);
    }

    public ParticipantResponseDto create(ParticipantRequestDto dto){
        Participant participant =  participantRepository.save(ParticipantMapper.toEntity(dto));
        return ParticipantMapper.toDto(participant);
    }

    @Transactional
    public ParticipantResponseDto update(UUID id, ParticipantRequestDto dto){
        Participant participant = participantRepository.findByIdAndIsDeletedFalseOrThrow(id);

        String firstName = dto.firstName();
        String lastName = dto.lastName();

        if (firstName != null && !firstName.equals(participant.getFirstName()))
            participant.setFirstName(firstName);

        if (lastName != null && !lastName.equals(participant.getLastName()))
            participant.setLastName(lastName);

        Participant updatedParticipant = participantRepository.save(participant);
        return ParticipantMapper.toDto(updatedParticipant);
    }

    @Transactional
    public void delete(UUID id){
        int updatedRows = participantRepository.softDeleteById(id);

        if (updatedRows == 0)
            throw new NotFoundException("Участника с id = " + id + " не существует");

        qrCodeRepository.softDeleteByParticipantId(id);
    }

    @Transactional(readOnly = true)
    public Participant getParticipantById(UUID id){
        return participantRepository.findByIdAndIsDeletedFalseOrThrow(id);
    }
}
