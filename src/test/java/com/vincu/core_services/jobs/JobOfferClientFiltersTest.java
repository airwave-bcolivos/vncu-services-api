package com.vincu.core_services.jobs;

import com.vincu.core_services.jobs.domain.JobStatus;
import com.vincu.core_services.jobs.dto.JobOfferSummaryResponse;
import com.vincu.core_services.jobs.dto.PageResponse;
import com.vincu.core_services.jobs.service.JobOfferService;
import com.vincu.core_services.jobs.web.JobOfferController;
import com.vincu.core_services.users.domain.Specialty;
import com.vincu.core_services.utils.StringToSpecialtyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
}, showSql = false)
@Import(JobOfferService.class)
class JobOfferClientFiltersTest {

    @Autowired private JobOfferService service;
    @Autowired private JdbcTemplate jdbc;

    private UUID clientId;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        UUID documentType = UUID.randomUUID();
        jdbc.update("INSERT INTO document_type (document_type_id, name, code) VALUES (?, 'DNI', 'DNI')",
                documentType);
        clientId = createClient(documentType);
        UUID otherClient = createClient(documentType);
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

        var conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new StringToSpecialtyConverter());
        mvc = MockMvcBuilders.standaloneSetup(new JobOfferController(service))
                .setConversionService(conversionService).build();
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

        var response = mvc.perform(get("/api/v1/jobs/{id}", offerId))
                .andExpect(status().isOk()).andReturn().getResponse();
        var body = JsonMapper.builder().build().readTree(response.getContentAsString());

        assertThat(body.path("ubigeoDistrictId").asText()).isEqualTo("150101");
        assertThat(body.path("ubigeoDistrictName").asText()).isEqualTo("Lima");
        assertThat(body.path("ubigeoProvinceId").asText()).isEqualTo("1501");
        assertThat(body.path("ubigeoProvinceName").asText()).isEqualTo("Lima provincia");
        assertThat(body.path("ubigeoDepartmentId").asText()).isEqualTo("15");
        assertThat(body.path("ubigeoDepartmentName").asText()).isEqualTo("Lima departamento");
        assertThat(body.path("specificAddress").isNull()).isTrue();
    }

    private PageResponse<JobOfferSummaryResponse> unfiltered(String field, Sort.Direction direction, int page, int size) {
        return service.getOffersByClient(clientId.toString(), null, null, null, null, null, null,
                field, direction, page, size);
    }

    private UUID createClient(UUID documentType) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO users (id, email, password, role) VALUES (?, ?, 'test', 'CLIENT')",
                id, id + "@example.com");
        jdbc.update("""
                INSERT INTO client_profiles (user_id, names, lastname, document_type_id, document_id)
                VALUES (?, 'Test', 'Client', ?, ?)
                """, id, documentType, id.toString());
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
