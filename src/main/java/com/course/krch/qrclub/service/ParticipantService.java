package com.course.krch.qrclub.service;

import com.course.krch.qrclub.dto.ParticipantRequestDto;
import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.entity.Participant;
import com.course.krch.qrclub.exception.NotFoundException;
import com.course.krch.qrclub.mapper.ParticipantMapper;
import com.course.krch.qrclub.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    private final ParticipantRepository repository;

    public ParticipantService(ParticipantRepository repository) {
        this.repository = repository;
    }

    public List<ParticipantResponseDto> getAll(){
        return repository.findAll()
                .stream()
                .map(ParticipantMapper::toDto)
                .toList();
    }

    public ParticipantResponseDto getById(UUID id){
        Participant participant = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Участника с id = " + id + " не существует")
                );
        return ParticipantMapper.toDto(participant);
    }

    public ParticipantResponseDto create(ParticipantRequestDto dto){
        Participant participant =  repository.save(ParticipantMapper.toEntity(dto));
        return ParticipantMapper.toDto(participant);
    }

    public ParticipantResponseDto update(UUID id, ParticipantRequestDto dto){
        Participant participant = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Участника с id = " + id + " не существует")
                );

        String firstName = dto.firstName();
        String lastName = dto.lastName();

        if (firstName != null && !firstName.equals(participant.getFirstName()))
            participant.setFirstName(firstName);

        if (lastName != null && !lastName.equals(participant.getLastName()))
            participant.setLastName(lastName);

        Participant updatedParticipant = repository.save(participant);
        return ParticipantMapper.toDto(updatedParticipant);
    }

    public void delete(UUID id){
        if (!repository.existsById(id))
            throw new NotFoundException("Участника с id = " + id + " не существует");

        repository.deleteById(id);
    }

    public Participant getParticipantById(UUID id){
        return repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Участника с id = " + id + " не существует")
                );
    }
}
