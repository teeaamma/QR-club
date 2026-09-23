package com.course.krch.qrclub.repository;

import com.course.krch.qrclub.entity.QrCode;
import com.course.krch.qrclub.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

    default QrCode findByIdAndIsDeletedFalseOrThrow(UUID id){
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new NotFoundException("QR-кода с id = " + id + " не существует")
                );
    }

    Page<QrCode> findAllByIsDeletedFalse(Pageable pageable);

    Optional<QrCode> findByIdAndIsDeletedFalse(UUID id);

    @Modifying
    @Query("""
            update QrCode q
            set q.isDeleted = true
            where q.id = :id
            and q.isDeleted = false
            """)
    int softDeleteById(UUID id);

    @Modifying
    @Query("""
            update QrCode q
            set q.isDeleted = true
            where q.participant.id = :id
            and q.isDeleted = false
            """)
    int softDeleteByParticipantId(UUID id);
}
