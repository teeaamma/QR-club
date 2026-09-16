package com.course.krch.qrclub.repository;

import com.course.krch.qrclub.entity.QrCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {
}
