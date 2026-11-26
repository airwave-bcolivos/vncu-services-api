package com.vincu.core_services.jobs;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.JobOfferSummaryResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.service.JobOfferService;
import com.vincu.core_services.users.domain.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "server.port=0",
        "spring.datasource.url=jdbc:h2:mem:jobs;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
@Transactional
class JobOfferClientFiltersTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder(4).encode("test");
    private static final String OFFERS_PATH = "/api/v1/jobs/{id}/diagnostic-offers";
    private static final String ACCEPT_PATH = OFFERS_PATH + "/{offerId}/accept";
    private static final String PENDING_JOBS_PATH = "/api/v1/jobs/diagnostic-offers/mine";

    @Autowired private JobOfferService service;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private MockMvc mvc;

    private UUID clientId;
    private UUID otherClient;
    private UUID documentType;

    @BeforeEach
    void setUp() {
        documentType = UUID.randomUUID();
        jdbc.update("INSERT INTO document_type (document_type_id, name, code) VALUES (?, 'DNI', 'DNI')",
                documentType);
        clientId = createClient(documentType);
        otherClient = createClient(documentType);
        jdbc.update("INSERT INTO ubigeo_departments (id, name) VALUES ('15', 'Lima departamento')");
        jdbc.update("INSERT INTO ubigeo_provinces (id, department_id, name) VALUES ('1501', '15', 'Lima provincia')");
        jdbc.update("INSERT INTO ubigeo_districts (id, province_id, name) VALUES ('150101', '1501', 'Lima')");

        offer(clientId, "first", "2026-11-05T00:00:00", "100", Specialty.GASFITERO, JobStatus.OPEN);
        offer(clientId, "last", "2026-11-05T23:59:59.999999", "200", Specialty.GASFITERO, JobStatus.OPEN);
        offer(clientId, "older", "2026-11-04T23:59:59", "300", Specialty.GASFITERO, JobStatus.OPEN);
        offer(clientId, "newer", "2026-11-06T00:00:00", "50", Specialty.GASFITERO, JobStatus.OPEN);
        offer(clientId, "specialty", "2026-11-05T12:00:00", "150", Specialty.PINTURA, JobStatus.OPEN);
        offer(clientId, "state", "2026-11-05T12:00:00", "150", Specialty.GASFITERO, JobStatus.COMPLETED);
        offer(clientId, "budget", "2026-11-05T12:00:00", "201", Specialty.GASFITERO, JobStatus.OPEN);
        offer(otherClient, "other client", "2026-11-05T12:00:00", "150", Specialty.GASFITERO, JobStatus.OPEN);

    }

    @Test
    void combinesFiltersWithInclusiveDateAndBudgetBounds() {
        var result = service.getOffersByClient(clientId.toString(), LocalDate.of(2026, 11, 5),
                LocalDate.of(2026, 11, 5), Specialty.GASFITERO, new BigDecimal("100"),
                new BigDecimal("200"), JobStatus.OPEN, "createdAt", Sort.Direction.ASC, 0, 10);

        assertThat(result.content()).extracting(JobOfferSummaryResponse::title).containsExactly("first", "last");
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.content()).extracting(JobOfferSummaryResponse::ubigeoDistrictName).containsOnly("Lima");
    }

    @ParameterizedTest
    @CsvSource({"createdAt,ASC,older", "createdAt,DESC,newer", "maxBudget,ASC,newer", "maxBudget,DESC,older"})
    void sortsBeforePagination(String field, Sort.Direction direction, String expectedFirst) {
        var result = unfiltered(field, direction, 0, 1);

        assertThat(result.content()).extracting(JobOfferSummaryResponse::title).containsExactly(expectedFirst);
        assertThat(result.totalElements()).isEqualTo(7);
        assertThat(result.totalPages()).isEqualTo(7);
    }

    @Test
    void supportsSingleSidedRangesAndOptionalFilters() {
        var from = service.getOffersByClient(clientId.toString(), LocalDate.of(2026, 11, 6), null,
                null, null, null, null, "createdAt", Sort.Direction.DESC, 0, 10);
        var to = service.getOffersByClient(clientId.toString(), null, LocalDate.of(2026, 11, 4),
                null, null, null, null, "createdAt", Sort.Direction.DESC, 0, 10);
        var min = service.getOffersByClient(clientId.toString(), null, null,
                null, new BigDecimal("300"), null, null, "createdAt", Sort.Direction.DESC, 0, 10);
        var max = service.getOffersByClient(clientId.toString(), null, null,
                null, null, new BigDecimal("50"), null, "createdAt", Sort.Direction.DESC, 0, 10);

        assertThat(from.content()).extracting(JobOfferSummaryResponse::title).containsExactly("newer");
        assertThat(to.content()).extracting(JobOfferSummaryResponse::title).containsExactly("older");
        assertThat(min.content()).extracting(JobOfferSummaryResponse::title).containsExactly("older");
        assertThat(max.content()).extracting(JobOfferSummaryResponse::title).containsExactly("newer");
    }

    @Test
    void retainsPaginationLimitsAndReturnsEmptyPages() {
        var result = unfiltered("createdAt", Sort.Direction.DESC, -1, 100);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(50);
        assertThat(result.content()).hasSize(7);
        assertThat(unfiltered("createdAt", Sort.Direction.DESC, 100, 10).content()).isEmpty();
    }

    @Test
    void bindsFiltersFromHttpAndKeepsExistingRequestsWorking() throws Exception {
        mvc.perform(get("/api/v1/jobs").param("clientId", clientId.toString()))
                .andExpect(status().isOk());
        var response = mvc.perform(get("/api/v1/jobs").param("clientId", clientId.toString())
                        .param("createdFrom", "2026-11-05").param("createdTo", "2026-11-05")
                        .param("specialty", "Gasfitero").param("status", "OPEN")
                        .param("minBudget", "100").param("maxBudget", "200")
                        .param("sortBy", "maxBudget").param("sortDirection", "ASC"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
        var body = JsonMapper.builder().build().readTree(response.getContentAsString());
        assertThat(body.path("totalElements").asInt()).isEqualTo(2);
        assertThat(body.path("content").get(0).path("title").asText()).isEqualTo("first");
        assertThat(body.path("content").get(1).path("title").asText()).isEqualTo("last");
    }

    @ParameterizedTest
    @CsvSource({
            "createdFrom,2026-11-06,createdTo,2026-11-05",
            "minBudget,200,maxBudget,100",
            "minBudget,-1,maxBudget,100",
            "maxBudget,-1,minBudget,0",
            "sortBy,title,size,10",
            "sortDirection,INVALID,size,10",
            "status,INVALID,size,10",
            "createdFrom,invalid,size,10"
    })
    void rejectsInvalidFilters(String key, String value, String otherKey, String otherValue) throws Exception {
        mvc.perform(get("/api/v1/jobs").param("clientId", clientId.toString())
                        .param(key, value).param(otherKey, otherValue))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detailReturnsDistrictProvinceAndDepartment() throws Exception {
        UUID offerId = jdbc.queryForObject("SELECT id FROM job_offers WHERE title = 'first'", UUID.class);

        var response = mvc.perform(get("/api/v1/jobs/{id}", offerId).param("viewerId", clientId.toString()))
                .andExpect(status().isOk()).andReturn().getResponse();
        var body = JsonMapper.builder().build().readTree(response.getContentAsString());

        assertThat(body.path("ubigeoDistrictId").asText()).isEqualTo("150101");
        assertThat(body.path("ubigeoDistrictName").asText()).isEqualTo("Lima");
        assertThat(body.path("ubigeoProvinceId").asText()).isEqualTo("1501");
        assertThat(body.path("ubigeoProvinceName").asText()).isEqualTo("Lima provincia");
        assertThat(body.path("ubigeoDepartmentId").asText()).isEqualTo("15");
        assertThat(body.path("ubigeoDepartmentName").asText()).isEqualTo("Lima departamento");
        assertThat(body.path("specificAddress").isNull()).isTrue();
        assertThat(body.path("clientPhone").isNull()).isTrue();
        assertThat(body.path("clientEmail").isNull()).isTrue();
        mvc.perform(withToken(get("/api/v1/jobs/{id}", offerId), login(clientId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specificAddress").value("Test address"));
    }

    @ParameterizedTest
    @EnumSource(JobStatus.class)
    void cancelsOfferWithoutDeletingItAndSupportsRepeatedRequests(JobStatus initialStatus) throws Exception {
        UUID offerId = jdbc.queryForObject("SELECT id FROM job_offers WHERE title = 'first'", UUID.class);
        jdbc.update("UPDATE job_offers SET status = ? WHERE id = ?", initialStatus.name(), offerId);

        String token = login(clientId);
        mvc.perform(withToken(delete("/api/v1/jobs/{id}", offerId), token)).andExpect(status().isNoContent());
        entityManager.flush();
        entityManager.clear();

        assertThat(jdbc.queryForObject("SELECT status FROM job_offers WHERE id = ?", String.class, offerId))
                .isEqualTo("CANCELED");
        assertThat(jdbc.queryForObject("SELECT title FROM job_offers WHERE id = ?", String.class, offerId))
                .isEqualTo("first");
        assertThat(service.getOpenOffers(null, null, null ,0, 50).content())
                .extracting(JobOfferSummaryResponse::id).doesNotContain(offerId.toString());

        mvc.perform(withToken(delete("/api/v1/jobs/{id}", offerId), token)).andExpect(status().isNoContent());
        entityManager.flush();
        assertThat(jdbc.queryForObject("SELECT status FROM job_offers WHERE id = ?", String.class, offerId))
                .isEqualTo("CANCELED");
    }

    @Test
    void cancelReturnsNotFoundForMissingOffer() throws Exception {
        mvc.perform(withToken(delete("/api/v1/jobs/{id}", UUID.randomUUID()), login(clientId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelRejectsInvalidId() throws Exception {
        mvc.perform(withToken(delete("/api/v1/jobs/invalid-id"), login(clientId))).andExpect(status().isBadRequest());
    }

    @Test
    void submitsListsAndAcceptsOnlyOneDiagnosticOfferForTheOwner() throws Exception {
        UUID jobId = jdbc.queryForObject("SELECT id FROM job_offers WHERE title = 'first'", UUID.class);
        UUID technician = createTechnician();
        mvc.perform(get("/api/v1/technicians/{id}", technician))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").value("Test"));
        String techToken = login(technician);
        String secondTechToken = login(createTechnician());
        String clientToken = login(clientId);
        String otherClientToken = login(otherClient);

        mvc.perform(get(PENDING_JOBS_PATH)).andExpect(status().isUnauthorized());
        mvc.perform(withToken(get(PENDING_JOBS_PATH), clientToken)).andExpect(status().isForbidden());
        mvc.perform(withToken(get(PENDING_JOBS_PATH), techToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());

        mvc.perform(get(OFFERS_PATH, jobId)).andExpect(status().isUnauthorized());
        mvc.perform(withToken(get(OFFERS_PATH, jobId), techToken)).andExpect(status().isForbidden());
        mvc.perform(withToken(get(OFFERS_PATH, jobId), otherClientToken)).andExpect(status().isForbidden());
        mvc.perform(withToken(delete("/api/v1/jobs/{id}", jobId), otherClientToken)).andExpect(status().isForbidden());
        mvc.perform(offerRequest(jobId, clientToken, "75.50", "Revisión")).andExpect(status().isForbidden());
        mvc.perform(offerRequest(jobId, techToken, "75.123", "Revisión")).andExpect(status().isBadRequest());
        mvc.perform(offerRequest(jobId, techToken, "75.50", " ")).andExpect(status().isBadRequest());

        var response = mvc.perform(offerRequest(jobId, techToken, "75.50", " Revisión "))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.technicianId").value(technician.toString()))
                .andExpect(jsonPath("$.description").value("Revisión"))
                .andExpect(jsonPath("$.status").value("PENDING")).andReturn().getResponse();
        String firstOfferId = JSON.readTree(response.getContentAsString()).path("id").asText();
        mvc.perform(withToken(get(PENDING_JOBS_PATH), techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(jobId.toString()))
                .andExpect(jsonPath("$.content[0].ubigeoDistrictName").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].specificAddress").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].clientPhone").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.content[0].clientEmail").doesNotHaveJsonPath());
        mvc.perform(withToken(get(PENDING_JOBS_PATH), secondTechToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(offerRequest(jobId, techToken, "80", "Otra oferta")).andExpect(status().isConflict());
        response = mvc.perform(offerRequest(jobId, secondTechToken, "90.25", "Segunda oferta"))
                .andExpect(status().isCreated()).andReturn().getResponse();
        String secondOfferId = JSON.readTree(response.getContentAsString()).path("id").asText();

        mvc.perform(withToken(get("/api/v1/jobs/{id}", jobId), techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnosticOfferSubmitted").value(true))
                .andExpect(jsonPath("$.clientPhone").isEmpty())
                .andExpect(jsonPath("$.clientEmail").isEmpty())
                .andExpect(jsonPath("$.specificAddress").isEmpty());
        mvc.perform(withToken(get(OFFERS_PATH, jobId), clientToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/jobs").param("clientId", clientId.toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[?(@.title == 'first')].propuestasCount").value(2));
        mvc.perform(withToken(patch(ACCEPT_PATH, jobId, firstOfferId), otherClientToken)).andExpect(status().isForbidden());
        mvc.perform(withToken(patch(ACCEPT_PATH, jobId, firstOfferId), clientToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACCEPTED"));
        mvc.perform(withToken(patch(ACCEPT_PATH, jobId, firstOfferId), clientToken)).andExpect(status().isOk());
        mvc.perform(withToken(patch(ACCEPT_PATH, jobId, secondOfferId), clientToken)).andExpect(status().isConflict());
        mvc.perform(offerRequest(jobId, login(createTechnician()), "100", "Otra oferta")).andExpect(status().isConflict());

        entityManager.clear();
        mvc.perform(withToken(get(PENDING_JOBS_PATH), techToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(withToken(get(PENDING_JOBS_PATH), secondTechToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content").isEmpty());
        mvc.perform(withToken(get("/api/v1/jobs/{id}", jobId), techToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.technicianId").value(technician.toString()))
                .andExpect(jsonPath("$.clientPhone").value("987654321"))
                .andExpect(jsonPath("$.clientEmail").value(clientId + "@example.com"))
                .andExpect(jsonPath("$.specificAddress").value("Test address"));
        mvc.perform(withToken(get("/api/v1/jobs/{id}", jobId), secondTechToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientPhone").isEmpty())
                .andExpect(jsonPath("$.clientEmail").isEmpty());
        mvc.perform(withToken(get(OFFERS_PATH, jobId), clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '%s')].status".formatted(secondOfferId)).value("REJECTED"));
    }

    private MockHttpServletRequestBuilder offerRequest(UUID jobId, String token, String price, String description) {
        return withToken(post(OFFERS_PATH, jobId), token).contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(java.util.Map.of(
                        "diagnosticPrice", new BigDecimal(price), "description", description)));
    }

    @ParameterizedTest
    @CsvSource({"CLIENT", "TECHNICIAN"})
    void eitherParticipantCanFinishAndBothCanReview(String finishingRole) throws Exception {
        UUID jobId = jdbc.queryForObject("SELECT id FROM job_offers WHERE title = 'first'", UUID.class);
        UUID technician = createTechnician();
        String techToken = login(technician);
        String clientToken = login(clientId);
        String otherClientToken = login(otherClient);
        String completion = "/api/v1/jobs/{id}/complete";
        String jobReview = "/api/v1/jobs/{id}/review";
        String clientReview = "/api/v1/client/{id}/client-review";

        mvc.perform(patch(completion, jobId)).andExpect(status().isUnauthorized());
        mvc.perform(withToken(patch(completion, jobId), clientToken)).andExpect(status().isConflict());

        var offer = mvc.perform(offerRequest(jobId, techToken, "50.00", "Diagnóstico"))
                .andExpect(status().isCreated()).andReturn().getResponse();
        String offerId = JSON.readTree(offer.getContentAsString()).path("id").asText();
        mvc.perform(withToken(patch(ACCEPT_PATH, jobId, offerId), clientToken)).andExpect(status().isOk());
        mvc.perform(withToken(patch(completion, jobId), otherClientToken)).andExpect(status().isForbidden());
        mvc.perform(reviewRequest(jobReview, jobId, clientToken, 5)).andExpect(status().isConflict());
        mvc.perform(reviewRequest(clientReview, jobId, techToken, 4)).andExpect(status().isConflict());

        boolean clientFinishes = finishingRole.equals("CLIENT");
        mvc.perform(withToken(patch(completion, jobId), clientFinishes ? clientToken : techToken))
                .andExpect(status().isNoContent());
        mvc.perform(withToken(patch(completion, jobId), clientFinishes ? techToken : clientToken))
                .andExpect(status().isNoContent());
        entityManager.flush();
        assertThat(jdbc.queryForObject("SELECT status FROM job_offers WHERE id = ?", String.class, jobId))
                .isEqualTo("COMPLETED");

        mvc.perform(reviewRequest(jobReview, jobId, otherClientToken, 5)).andExpect(status().isForbidden());
        mvc.perform(reviewRequest(jobReview, jobId, clientToken, 5)).andExpect(status().isCreated());
        mvc.perform(reviewRequest(clientReview, jobId, techToken, 4)).andExpect(status().isCreated());
        mvc.perform(reviewRequest(jobReview, jobId, clientToken, 3)).andExpect(status().isConflict());
        mvc.perform(reviewRequest(clientReview, jobId, techToken, 3)).andExpect(status().isConflict());
        mvc.perform(withToken(get(jobReview, jobId), clientToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rating").value(5));
        mvc.perform(withToken(get(clientReview, jobId), techToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rating").value(4));
        assertThat(jdbc.queryForObject("SELECT rating_count FROM technician_profiles WHERE user_id = ?", Integer.class, technician))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT rating_count FROM client_profiles WHERE user_id = ?", Integer.class, clientId))
                .isEqualTo(1);
    }

    private MockHttpServletRequestBuilder reviewRequest(String path, UUID jobId, String token, int rating) {
        return withToken(post(path, jobId), token).contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(java.util.Map.of("rating", rating, "comment", "Buen servicio")));
    }

    private String login(UUID id) throws Exception {
        String email = jdbc.queryForObject("SELECT email FROM users WHERE id = ?", String.class, id);
        var response = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"test"}
                                """.formatted(email)))
                .andExpect(status().isOk()).andReturn().getResponse();
        return JSON.readTree(response.getContentAsString()).path("token").asText();
    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header("Authorization", "Bearer " + token);
    }

    private PageResponse<JobOfferSummaryResponse> unfiltered(String field, Sort.Direction direction, int page, int size) {
        return service.getOffersByClient(clientId.toString(), null, null, null, null, null, null,
                field, direction, page, size);
    }

    private UUID createClient(UUID documentType) {
        UUID id = createUser("CLIENT");
        jdbc.update("""
                INSERT INTO client_profiles (user_id, names, lastname, document_type_id, document_id, rating_count, phone_number)
                VALUES (?, 'Test', 'Client', ?, ?, 0, '987654321')
                """, id, documentType, id.toString());
        return id;
    }

    private UUID createTechnician() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/v1/users/register/technician")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"test","names":"Test","lastname":"Tech",
                                 "documentTypeCode":"DNI","documentId":"%s","specialty":"GASFITERO",
                                 "bioDescription":"Diagnóstico de instalaciones"}
                                """.formatted(email, UUID.randomUUID())))
                .andExpect(status().isCreated());
        entityManager.flush();
        return jdbc.queryForObject("SELECT id FROM users WHERE email = ?", UUID.class, email);
    }

    private UUID createUser(String role) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO users (id, email, password, role) VALUES (?, ?, ?, ?)",
                id, id + "@example.com", PASSWORD_HASH, role);
        return id;
    }

    private void offer(UUID owner, String title, String createdAt, String budget, Specialty specialty, JobStatus status) {
        jdbc.update("""
                INSERT INTO job_offers (id, client_id, title, specialty, description, max_budget, status,
                    ubigeo_district_id, specific_address, latitude, longitude, created_at)
                VALUES (?, ?, ?, ?, 'Test', ?, ?, '150101', 'Test address', 0, 0, ?)
                """, UUID.randomUUID(), owner, title, specialty.name(), new BigDecimal(budget), status.name(),
                java.sql.Timestamp.valueOf(createdAt.replace('T', ' ')));
    }
}
