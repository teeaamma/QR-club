package com.course.krch.qrclub.repository;

import com.course.krch.qrclub.entity.Participant;
import com.course.krch.qrclub.exception.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID>, JpaSpecificationExecutor<Participant> {

    default Participant findByIdAndIsDeletedFalseOrThrow(UUID id){
        return findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new NotFoundException("Участника с id = " + id + " не существует")
                );
    }

    Optional<Participant> findByIdAndIsDeletedFalse(UUID id);

    @Modifying
    @Query("""
            update Participant p
            set p.isDeleted = true
            where p.id = :id
            and p.isDeleted = false
            """)
    int softDeleteById(UUID id);
}
