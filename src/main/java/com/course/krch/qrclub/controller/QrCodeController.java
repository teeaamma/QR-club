package com.course.krch.qrclub.controller;

import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.dto.QrCodeRequestDto;
import com.course.krch.qrclub.dto.QrCodeResponseDto;
import com.course.krch.qrclub.service.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr-codes")
public class QrCodeController {

    private final QrCodeService service;

    public QrCodeController(QrCodeService service) {
        this.service = service;
    }

    @GetMapping
    public Page<QrCodeResponseDto> getAll(Pageable pageable){
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    public QrCodeResponseDto getById(@PathVariable UUID id){
        return service.getById(id);
    }

    @PostMapping
    public QrCodeResponseDto create(@Valid @RequestBody QrCodeRequestDto dto){
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public QrCodeResponseDto update(
            @PathVariable UUID id,
            @Valid @RequestBody QrCodeRequestDto dto
    ){
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        service.delete(id);
    }

    @PostMapping("/{id}/scan")
    public ParticipantResponseDto scan(@PathVariable UUID id){
        return service.scan(id);
    }
}
