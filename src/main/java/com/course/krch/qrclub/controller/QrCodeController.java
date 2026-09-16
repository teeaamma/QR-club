package com.course.krch.qrclub.controller;

import com.course.krch.qrclub.dto.ParticipantResponseDto;
import com.course.krch.qrclub.dto.QrCodeRequestDto;
import com.course.krch.qrclub.dto.QrCodeResponseDto;
import com.course.krch.qrclub.service.QrCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/qr-codes")
public class QrCodeController {

    private final QrCodeService service;

    public QrCodeController(QrCodeService service) {
        this.service = service;
    }

    @GetMapping
    public List<QrCodeResponseDto> getAll(){
        return service.getAll();
    }

    @GetMapping("/{id}")
    public QrCodeResponseDto getById(@PathVariable UUID id){
        return service.getById(id);
    }

    @PostMapping
    public QrCodeResponseDto create(@RequestBody QrCodeRequestDto dto){
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public QrCodeResponseDto update(
            @PathVariable UUID id,
            @RequestBody QrCodeRequestDto dto
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
