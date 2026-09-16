package com.course.krch.qrclub.controller;


import com.course.krch.qrclub.dto.ParticipantRequestDto;
import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.service.ParticipantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService service;

    public ParticipantController(ParticipantService service) {
        this.service = service;
    }

    @GetMapping
    public List<ParticipantResponseDto> getAll(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ParticipantResponseDto getById(@PathVariable UUID id){
        return service.getById(id);
    }

    @PostMapping
    public ParticipantResponseDto create(@RequestBody ParticipantRequestDto dto){
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ParticipantResponseDto update(
            @PathVariable UUID id,
            @RequestBody ParticipantRequestDto dto
            ){
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        service.delete(id);
    }
}
