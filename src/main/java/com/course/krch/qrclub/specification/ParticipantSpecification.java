package com.course.krch.qrclub.specification;

import com.course.krch.qrclub.entity.Participant;
import org.springframework.data.jpa.domain.Specification;

public final class ParticipantSpecification {

    private ParticipantSpecification(){}

    public static Specification<Participant> notDeleted(){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    public static Specification<Participant> firstNameContains(String firstName){
        if (firstName == null || firstName.isBlank()) {
            return Specification.unrestricted();
        }

        String value = "%" + firstName.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        value
                );
    }

    public static Specification<Participant> lastNameContains(String lastName){
        if (lastName == null || lastName.isBlank()) {
            return Specification.unrestricted();
        }

        String value = "%" + lastName.trim().toLowerCase() + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        value
                );
    }
}
