package com.course.krch.qrclub;

import com.course.krch.qrclub.repository.ParticipantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = "spring.liquibase.contexts=test")
@AutoConfigureMockMvc
@Testcontainers
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
public class ParticipantIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonMapper jsonMapper;

    @Autowired
    ParticipantRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_defaultPagination() throws Exception {
        mockMvc.perform(get("/api/v1/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_requestedPage() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("page", "2")
                        .param("size", "1")
                        .param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Владимир"));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_lastPartialPage() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.content[0].firstName").value("Егор"));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_firstFilter() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("firstName", "ор"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].firstName", containsInAnyOrder(
                        "Борис",
                        "Егор"
                )));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_secondFilter() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("lastName", "ров"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].lastName", containsInAnyOrder(
                        "Владимиров",
                        "Егоров"
                )));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_bothFilters() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("firstName", "ор")
                        .param("lastName", "ров"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName").value("Егор"));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_noFilterMatches() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("firstName", "фильтр")
                        .param("lastName", "фильтр"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.numberOfElements").value(0));
    }

    @Test
    @Sql("/sql/participant/list.sql")
    void getAll_filteredPagination() throws Exception {
        mockMvc.perform(get("/api/v1/participants")
                        .param("page", "1")
                        .param("size", "1")
                        .param("sort", "firstName,asc")
                        .param("firstName", "ор")
                        .param("lastName", "ов"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Егор"));
    }

    @Test
    @Sql("/sql/participant/item.sql")
    void getById_existingId() throws Exception {
        mockMvc.perform(get(
                "/api/v1/participants/{id}",
                "11111111-1111-4111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-4111-1111-111111111111"))
                .andExpect(jsonPath("$.firstName").value("Андрей"))
                .andExpect(jsonPath("$.lastName").value("Андреев"));
    }

    @Test
    void getById_nonExistingId() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/participants/{id}",
                        "66666666-6666-4666-6666-666666666666"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Участника с id = 66666666-6666-4666-6666-666666666666 не существует"
                ));
    }

    @Test
    void getById_invalidIdFormat() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/participants/{id}",
                        "invalid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат аргумента id"));
    }

    @Test
    @Sql("/sql/participant/deleted-item.sql")
    void getById_deletedId() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/participants/{id}",
                        "11111111-1111-4111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Участника с id = 11111111-1111-4111-1111-111111111111 не существует"
                ));
    }

    @Test
    void create_validRequest() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "Андрей")
                .put("lastName", "Андреев");

        var result = mockMvc.perform(post("/api/v1/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Андрей"))
                .andExpect(jsonPath("$.lastName").value("Андреев"))
                .andReturn();

        var responseBody = jsonMapper.readTree(result.getResponse().getContentAsString());
        var createdId = UUID.fromString(responseBody.get("id").asString());

        var savedParticipant = repository.findById(createdId).orElseThrow();

        assertThat(savedParticipant.getFirstName()).isEqualTo("Андрей");
        assertThat(savedParticipant.getLastName()).isEqualTo("Андреев");
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "NULL, Андреев",
                    "'', Андреев",
                    "' ', Андреев"
            },
            nullValues = "NULL"
    )
    void create_invalidFirstName(String firstName, String lastName) throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", firstName)
                .put("lastName", lastName);

        mockMvc.perform(post("/api/v1/participants")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").value(hasSize(1)))
                .andExpect(jsonPath("$.violations[0].fieldName").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("не должно быть пустым"));
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "Андрей, NULL",
                    "Андрей, ''",
                    "Андрей, ' '"
            },
            nullValues = "NULL"
    )
    void create_invalidLastName(String firstName, String lastName) throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", firstName)
                .put("lastName", lastName);

        mockMvc.perform(post("/api/v1/participants")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").value(hasSize(1)))
                .andExpect(jsonPath("$.violations[0].fieldName").value("lastName"))
                .andExpect(jsonPath("$.violations[0].message").value("не должно быть пустым"));
    }

    @Test
    void create_invalidFields() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "")
                .put("lastName", "");

        mockMvc.perform(post("/api/v1/participants")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").value(hasSize(2)))
                .andExpect(jsonPath("$.violations[*].fieldName", containsInAnyOrder(
                        "firstName",
                        "lastName"
                )))
                .andExpect(jsonPath("$.violations[*].message").value(
                        everyItem(is("не должно быть пустым"))
                ));
    }

    @Test
    @Sql("/sql/participant/item.sql")
    void update_validRequest() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "Антон")
                .put("lastName", "Антонов");

        mockMvc.perform(put(
                "/api/v1/participants/{id}",
                "11111111-1111-4111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("11111111-1111-4111-1111-111111111111"))
                .andExpect(jsonPath("$.firstName").value("Антон"))
                .andExpect(jsonPath("$.lastName").value("Антонов"));

        var updatedParticipant = repository.findById(
                UUID.fromString("11111111-1111-4111-1111-111111111111")
        ).orElseThrow();

        assertThat(updatedParticipant.getFirstName()).isEqualTo("Антон");
        assertThat(updatedParticipant.getLastName()).isEqualTo("Антонов");
    }

    @Test
    void update_nonExistingId() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "Антон")
                .put("lastName", "Антонов");

        mockMvc.perform(put(
                        "/api/v1/participants/{id}",
                        "11111111-1111-4111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Участника с id = 11111111-1111-4111-1111-111111111111 не существует"
                ));
    }

    @Test
    void update_invalidIdFormat() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "Антон")
                .put("lastName", "Антонов");

        mockMvc.perform(put(
                        "/api/v1/participants/{id}",
                        "invalid-format")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Неправильный формат аргумента id"
                ));
    }

    @Test
    @Sql("/sql/participant/item.sql")
    void update_invalidBody() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "")
                .put("lastName", "Антонов");

        mockMvc.perform(put(
                        "/api/v1/participants/{id}",
                        "11111111-1111-4111-1111-111111111111")
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations").value(hasSize(1)))
                .andExpect(jsonPath("$.violations[0].fieldName").value("firstName"))
                .andExpect(jsonPath("$.violations[0].message").value("не должно быть пустым"));
    }

    @Test
    @Sql("/sql/participant/deleted-item.sql")
    void update_deletedId() throws Exception {
        var requestBody = jsonMapper.createObjectNode()
                .put("firstName", "Антон")
                .put("lastName", "Антонов");

        mockMvc.perform(put(
                        "/api/v1/participants/{id}",
                        "11111111-1111-4111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Участника с id = 11111111-1111-4111-1111-111111111111 не существует"
                ));
    }

    @Test
    @Sql({
            "/sql/participant/item.sql",
            "/sql/participant/qr-codes.sql"
    })
    void delete_existingId() throws Exception {
        mockMvc.perform(delete(
                "/api/v1/participants/{id}",
                "11111111-1111-4111-1111-111111111111"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/participants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(hasSize(0)));

        Boolean isDeleted = jdbcTemplate.queryForObject(
                "SELECT is_deleted FROM participants WHERE id = ?",
                Boolean.class,
                UUID.fromString("11111111-1111-4111-1111-111111111111")
        );

        Long deletedQrCodeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qr_codes WHERE participant_id = ? AND is_deleted = true",
                Long.class,
                UUID.fromString("11111111-1111-4111-1111-111111111111")
        );

        assertThat(isDeleted).isTrue();
        assertThat(deletedQrCodeCount).isEqualTo(2L);
    }

    @Test
    void delete_nonExistingId() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/participants/{id}",
                        "11111111-1111-4111-1111-111111111111"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Участника с id = 11111111-1111-4111-1111-111111111111 не существует"
                ));
    }

    @Test
    void delete_invalidIdFormat() throws Exception {
        mockMvc.perform(delete(
                        "/api/v1/participants/{id}",
                        "invalid-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Неправильный формат аргумента id"));
    }
}
