package com.course.krch.qrclub.service;

import com.course.krch.qrclub.dto.ParticipantRequestDto;
import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.entity.Participant;
import com.course.krch.qrclub.exception.NotFoundException;
import com.course.krch.qrclub.mapper.ParticipantMapper;
import com.course.krch.qrclub.repository.ParticipantRepository;
import com.course.krch.qrclub.specification.ParticipantSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository repository;

    public ParticipantService(ParticipantRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ParticipantResponseDto> getAll(String firstName, String lastName, Pageable pageable){

        Specification<Participant> specification = Specification.allOf(
                ParticipantSpecification.firstNameContains(firstName),
                ParticipantSpecification.lastNameContains(lastName),
                ParticipantSpecification.notDeleted()
        );

        return repository.findAll(specification, pageable)
                .map(ParticipantMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ParticipantResponseDto getById(UUID id){
        Participant participant = repository.findByIdAndIsDeletedFalseOrThrow(id);
        return ParticipantMapper.toDto(participant);
    }

    public ParticipantResponseDto create(ParticipantRequestDto dto){
        Participant participant =  repository.save(ParticipantMapper.toEntity(dto));
        return ParticipantMapper.toDto(participant);
    }

    @Transactional
    public ParticipantResponseDto update(UUID id, ParticipantRequestDto dto){
        Participant participant = repository.findByIdAndIsDeletedFalseOrThrow(id);

        String firstName = dto.firstName();
        String lastName = dto.lastName();

        if (firstName != null && !firstName.equals(participant.getFirstName()))
            participant.setFirstName(firstName);

        if (lastName != null && !lastName.equals(participant.getLastName()))
            participant.setLastName(lastName);

        Participant updatedParticipant = repository.save(participant);
        return ParticipantMapper.toDto(updatedParticipant);
    }

    @Transactional
    public void delete(UUID id){
        int updatedRows = repository.softDeleteById(id);

        if (updatedRows == 0)
            throw new NotFoundException("Участника с id = " + id + " не существует");
    }

    @Transactional(readOnly = true)
    public Participant getParticipantById(UUID id){
        return repository.findByIdAndIsDeletedFalseOrThrow(id);
    }
}
