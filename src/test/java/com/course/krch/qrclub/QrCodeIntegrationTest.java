package com.course.krch.qrclub;

import com.course.krch.qrclub.repository.QrCodeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QrCodeIntegrationTest extends AbstractIntegrationTest{

    @Autowired
    QrCodeRepository repository;

    @Test
    @Sql("/sql/qr-code/list.sql")
    void getAll_defaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/qr-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @Sql("/sql/qr-code/list.sql")
    void getAll_requestedPage() throws Exception {
        mockMvc.perform(get("/api/v1/qr-codes")
                        .param("page", "2")
                        .param("size", "1")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].id").value(
                        "55555555-5555-4555-5555-555555555555"
                ));
    }

    @Test
    @Sql("/sql/qr-code/list.sql")
    void getAll_lastPartialPage() throws Exception {
        mockMvc.perform(get("/api/v1/qr-codes")
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].id").value(
                        "77777777-7777-4777-7777-777777777777"
                ));
    }

    @Test
    @Sql("/sql/qr-code/item.sql")
    void getById_existingId() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("22222222-2222-4222-2222-222222222222"))
                .andExpect(jsonPath("$.userId").value("11111111-1111-4111-1111-111111111111"));
    }

    @Test
    void getById_nonExistingId() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/qr-codes/{id}",
                        "00000000-0000-4000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 00000000-0000-4000-0000-000000000000 не существует"
                ));
    }

    @Test
    void getById_invalidIdFormat() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/qr-codes/{id}",
                        "invalid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат аргумента id"));
    }

    @Test
    @Sql("/sql/participant/deleted-item.sql")
    void getById_deletedId() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 22222222-2222-4222-2222-222222222222 не существует"
                ));
    }

    @Test
    @Sql("/sql/qr-code/participant.sql")
    void create_validRequest() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "33333333-3333-4333-3333-333333333333");

        var result = mockMvc.perform(post("/api/v1/qr-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value("33333333-3333-4333-3333-333333333333"))
                .andReturn();

        var responseBody = jsonMapper.readTree(result.getResponse().getContentAsString());
        var createdId = UUID.fromString(responseBody.get("id").asString());

        var savedQrCode = repository.findById(createdId).orElseThrow();

        assertThat(savedQrCode.getParticipant().getId()).isEqualTo(
                UUID.fromString("33333333-3333-4333-3333-333333333333")
        );
    }

    @Test
    void create_userIdIsNull() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .putNull("userId");

        mockMvc.perform(post("/api/v1/qr-codes")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").value(hasSize(1)))
                .andExpect(jsonPath("$.violations[0].fieldName").value("userId"))
                .andExpect(jsonPath("$.violations[0].message").value(
                        "не должно равняться null"
                ));
    }

    @Test
    void create_invalidUserId() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "invalid-format");

        mockMvc.perform(post("/api/v1/qr-codes")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат тела запроса"));
    }

    @Test
    @Sql({
            "/sql/qr-code/item.sql",
            "/sql/qr-code/participant.sql"
    })
    void update_validRequest() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "33333333-3333-4333-3333-333333333333");

        mockMvc.perform(put(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("22222222-2222-4222-2222-222222222222"))
                .andExpect(jsonPath("$.userId").value("33333333-3333-4333-3333-333333333333"));

        var updatedQrCode = repository.findById(
                UUID.fromString("22222222-2222-4222-2222-222222222222")
        ).orElseThrow();

        assertThat(updatedQrCode.getParticipant().getId()).isEqualTo(
                UUID.fromString("33333333-3333-4333-3333-333333333333")
        );
    }

    @Test
    @Sql("/sql/qr-code/participant.sql")
    void update_nonExistingId() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "33333333-3333-4333-3333-333333333333");

        mockMvc.perform(put(
                        "/api/v1/qr-codes/{id}",
                        "11111111-1111-4111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 11111111-1111-4111-1111-111111111111 не существует"
                ));
    }

    @Test
    @Sql("/sql/qr-code/participant.sql")
    void update_invalidIdFormat() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "33333333-3333-4333-3333-333333333333");

        mockMvc.perform(put(
                        "/api/v1/qr-codes/{id}",
                        "invalid-format")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Неправильный формат аргумента id"
                ));
    }

    @Test
    @Sql("/sql/qr-code/item.sql")
    void update_invalidBody() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "invalid-format");

        mockMvc.perform(put(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат тела запроса"));
    }

    @Test
    @Sql({
            "/sql/qr-code/deleted-item.sql",
            "/sql/qr-code/participant.sql"
    })
    void update_deletedId() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("userId", "33333333-3333-4333-3333-333333333333");

        mockMvc.perform(put(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 22222222-2222-4222-2222-222222222222 не существует"
                ));
    }

    @Test
    @Sql("/sql/qr-code/item.sql")
    void delete_existingId() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/qr-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(hasSize(0)));

        Boolean isDeleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM qr_codes WHERE id = ?",
                Boolean.class,
                UUID.fromString("22222222-2222-4222-2222-222222222222")
        );

        assertThat(isDeleted).isTrue();
    }

    @Test
    void delete_nonExistingId() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/qr-codes/{id}",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 22222222-2222-4222-2222-222222222222 не существует"
                ));
    }

    @Test
    void delete_invalidIdFormat() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/qr-codes/{id}",
                        "invalid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат аргумента id"));
    }

    @Test
    @Sql("/sql/qr-code/item.sql")
    void scan_activeQrCode() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/qr-codes/{id}/scan",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-4111-1111-111111111111"))
                .andExpect(jsonPath("$.firstName").value("Андрей"))
                .andExpect(jsonPath("$.lastName").value("Андреев"));

        mockMvc.perform(get("/api/v1/qr-codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(
                        not("22222222-2222-4222-2222-222222222222")
                ))
                .andExpect(jsonPath("$.content[0].userId").value(
                        "11111111-1111-4111-1111-111111111111"
                ));
    }

    @Test
    @Sql("/sql/qr-code/item.sql")
    void scan_usedQrCode() throws Exception {
        mockMvc.perform(post(
                        "/api/v1/qr-codes/{id}/scan",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isOk());

        mockMvc.perform(post(
                        "/api/v1/qr-codes/{id}/scan",
                        "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 22222222-2222-4222-2222-222222222222 не существует"
                ));
    }

    @Test
    void scan_nonExistingId() throws Exception {
        mockMvc.perform(post(
                "/api/v1/qr-codes/{id}/scan",
                "22222222-2222-4222-2222-222222222222"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "QR-кода с id = 22222222-2222-4222-2222-222222222222 не существует"
                ));
    }

    @Test
    void scan_invalidIdFormat() throws Exception {
        mockMvc.perform(post(
                "/api/v1/qr-codes/{id}/scan",
                "invalid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат аргумента id"));
    }
}
