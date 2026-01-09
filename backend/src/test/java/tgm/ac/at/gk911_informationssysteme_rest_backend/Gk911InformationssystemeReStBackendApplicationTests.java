package tgm.ac.at.gk911_informationssysteme_rest_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.config.AuthController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.config.SecurityConfig;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.AnalysisController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Analysis;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.SampleId;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.AnalysisRepository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnalysisController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class Gk911InformationssystemeReStBackendApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AnalysisRepository repository;

    @MockBean
    SampleRepository sampleRepository;

    @MockBean
    AuthController authController;

    Analysis a1, a2, a3;
    private final java.util.concurrent.atomic.AtomicLong idGen = new java.util.concurrent.atomic.AtomicLong(100);
    private final Map<Long, Analysis> store = new java.util.concurrent.ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        // Reset mock behavior and in-memory store
        org.mockito.Mockito.reset(repository, sampleRepository);
        store.clear();

        a1 = new Analysis();
        a1.setId(idGen.incrementAndGet());
        a1.setComment("first");
        a1.setPol(new BigDecimal("1.23"));
        a1.setSId("S1");
        a1.setSStamp(LocalDateTime.parse("2024-01-01T10:00:00"));
        store.put(a1.getId(), a1);

        a2 = new Analysis();
        a2.setId(idGen.incrementAndGet());
        a2.setComment("second");
        a2.setGlu(new BigDecimal("2.34"));
        a2.setLane(2);
        a2.setSId("S2");
        a2.setSStamp(LocalDateTime.parse("2024-02-02T12:00:00"));
        store.put(a2.getId(), a2);

        a3 = new Analysis();
        a3.setId(idGen.incrementAndGet());
        a3.setComment("third");
        a3.setNat(new BigDecimal("3.45"));
        a3.setLane(3);
        a3.setAFlags("abc");
        a3.setSId("S3");
        a3.setSStamp(LocalDateTime.parse("2024-03-03T14:00:00"));
        store.put(a3.getId(), a3);

        // Mock sampleRepository to accept any sample id
        org.mockito.Mockito.when(sampleRepository.existsById(org.mockito.ArgumentMatchers.any(SampleId.class)))
                .thenReturn(true);

        // Default repository behavior delegates to our in-memory store
        org.mockito.Mockito.when(repository.findById(org.mockito.ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> java.util.Optional.ofNullable(store.get(inv.getArgument(0))));

        org.mockito.Mockito.when(repository.existsById(org.mockito.ArgumentMatchers.anyLong()))
                .thenAnswer(inv -> store.containsKey(inv.getArgument(0)));

        org.mockito.Mockito.when(repository.save(org.mockito.ArgumentMatchers.any(Analysis.class)))
                .thenAnswer(inv -> {
                    Analysis a = inv.getArgument(0);
                    if (a.getId() == null) {
                        a.setId(idGen.incrementAndGet());
                    }

                    // Copy-like behavior: replace existing with provided
                    Analysis copy = new Analysis();
                    copy.setId(a.getId());
                    copy.setComment(a.getComment());
                    copy.setLane(a.getLane());
                    copy.setPol(a.getPol());
                    copy.setNat(a.getNat());
                    copy.setKal(a.getKal());
                    copy.setAn(a.getAn());
                    copy.setGlu(a.getGlu());
                    copy.setDry(a.getDry());
                    copy.setDateIn(a.getDateIn());
                    copy.setDateOut(a.getDateOut());
                    copy.setWeightMea(a.getWeightMea());
                    copy.setWeightNrm(a.getWeightNrm());
                    copy.setWeightCur(a.getWeightCur());
                    copy.setWeightDif(a.getWeightDif());
                    copy.setDensity(a.getDensity());
                    copy.setAFlags(a.getAFlags());
                    copy.setDateExported(a.getDateExported());
                    copy.setSId(a.getSId());
                    copy.setSStamp(a.getSStamp());

                    store.put(copy.getId(), copy);
                    return copy;
                });

        org.mockito.Mockito.doAnswer(inv -> {
            Long id = inv.getArgument(0);
            store.remove(id);
            return null;
        }).when(repository).deleteById(org.mockito.ArgumentMatchers.anyLong());

        org.mockito.Mockito.when(repository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenAnswer(inv -> {
                    org.springframework.data.domain.Pageable p = inv.getArgument(0);
                    java.util.List<Analysis> all = new java.util.ArrayList<>(store.values());

                    // apply sort (only id supported here)
                    p.getSort().forEach(order -> {
                        if ("id".equalsIgnoreCase(order.getProperty())) {
                            all.sort((x, y) -> order.isAscending() ? Long.compare(x.getId(), y.getId()) : Long.compare(y.getId(), x.getId()));
                        }
                    });

                    int start = (int) p.getOffset();
                    int end = Math.min(start + p.getPageSize(), all.size());
                    java.util.List<Analysis> content = start >= all.size() ? java.util.Collections.emptyList() : all.subList(start, end);
                    return new org.springframework.data.domain.PageImpl<>(content, p, all.size());
                });
    }

    @AfterEach
    void tearDown() {
        store.clear();
        org.mockito.Mockito.reset(repository, sampleRepository);
    }

    private String toJson(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Test
    @DisplayName("GET /api/analysis returns 200 and a page of results")
    @WithMockUser
    void list_ok() throws Exception {
        mockMvc.perform(get("/api/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("GET list supports pagination params")
    @WithMockUser
    void list_pagination() throws Exception {
        mockMvc.perform(get("/api/analysis").param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/analysis").param("size", "2").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    @DisplayName("GET list empty page returns 200 and empty content")
    @WithMockUser
    void list_empty_page() throws Exception {
        store.clear();
        mockMvc.perform(get("/api/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("List sorting by id desc works via param")
    @WithMockUser
    void list_sorting_desc() throws Exception {
        mockMvc.perform(get("/api/analysis").param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].a_id").value(a3.getId()))
                .andExpect(jsonPath("$.content[2].a_id").value(a1.getId()));
    }

    @Test
    @DisplayName("GET by id returns 200 for existing")
    @WithMockUser
    void get_existing_ok() throws Exception {
        mockMvc.perform(get("/api/analysis/{id}", a2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.a_id").value(a2.getId()))
                .andExpect(jsonPath("$.comment").value("second"));
    }

    @Test
    @DisplayName("GET by id returns 404 for missing")
    @WithMockUser
    void get_missing_404() throws Exception {
        mockMvc.perform(get("/api/analysis/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET invalid id format returns 400 (type mismatch)")
    @WithMockUser
    void get_invalid_id_format() throws Exception {
        mockMvc.perform(get("/api/analysis/{id}", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST create returns 201 with Location and body contains id")
    @WithMockUser
    void post_create_201() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", "created");
        payload.put("lane", 7);
        payload.put("pol", new BigDecimal("5.55"));
        payload.put("s_id", "S100");
        payload.put("s_stamp", "2024-12-01T10:00:00");

        String res = mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/analysis/\\d+")))
                .andExpect(jsonPath("$.a_id").exists())
                .andExpect(jsonPath("$.comment").value("created"))
                .andReturn().getResponse().getContentAsString();

        Analysis created = objectMapper.readValue(res, Analysis.class);
        assertThat(repository.findById(created.getId())).isPresent();
    }

    @Test
    @DisplayName("POST with id returns 400 Bad Request")
    @WithMockUser
    void post_with_id_400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("a_id", 123);
        payload.put("comment", "invalid");

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST with long aFlags currently accepted (no validation)")
    @WithMockUser
    void post_invalid_aFlags_accepts() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("aFlags", "1234567890abcdef");
        payload.put("s_id", "S101");
        payload.put("s_stamp", "2024-12-02T10:00:00");

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.a_id").exists());
    }

    @Test
    @DisplayName("POST with too long comment fails 400 (validation)")
    @WithMockUser
    void post_invalid_comment_400() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", "x".repeat(300));
        payload.put("s_id", "S102");
        payload.put("s_stamp", "2024-12-03T10:00:00");

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST without Content-Type yields 415")
    @WithMockUser
    void post_without_content_type() throws Exception {
        mockMvc.perform(post("/api/analysis")
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("Unsupported media type returns 415 on POST")
    @WithMockUser
    void post_unsupported_media_type() throws Exception {
        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("POST payload with nulls is accepted (optional fields)")
    @WithMockUser
    void post_with_nulls() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", null);
        payload.put("lane", null);
        payload.put("s_id", "S103");
        payload.put("s_stamp", "2024-12-04T10:00:00");

        mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.a_id").exists());
    }

    @Test
    @DisplayName("PUT update existing returns 200 and persists changes")
    @WithMockUser
    void put_update_200() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", "updated");
        payload.put("lane", 9);

        mockMvc.perform(put("/api/analysis/{id}", a1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("updated"))
                .andExpect(jsonPath("$.lane").value(9));

        assertThat(repository.findById(a1.getId())).get().extracting(Analysis::getComment).isEqualTo("updated");
    }

    @Test
    @DisplayName("PUT missing id returns 404")
    @WithMockUser
    void put_missing_404() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", "does-not-exist");

        mockMvc.perform(put("/api/analysis/{id}", 123456789)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT with long aFlags currently accepted (no validation)")
    @WithMockUser
    void put_invalid_aFlags_accepts() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("aFlags", "1234567890abcdefg");

        mockMvc.perform(put("/api/analysis/{id}", a1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT invalid id format returns 400 (type mismatch)")
    @WithMockUser
    void put_invalid_id_format() throws Exception {
        mockMvc.perform(put("/api/analysis/{id}", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update can clear fields (set null)")
    @WithMockUser
    void put_clear_fields() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", null);
        payload.put("lane", null);

        mockMvc.perform(put("/api/analysis/{id}", a2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").isEmpty())
                .andExpect(jsonPath("$.lane").isEmpty());
    }

    @Test
    @DisplayName("DELETE existing returns 204 and removes entity")
    @WithMockUser
    void delete_ok() throws Exception {
        mockMvc.perform(delete("/api/analysis/{id}", a1.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.existsById(a1.getId())).isFalse();
    }

    @Test
    @DisplayName("DELETE missing returns 404")
    @WithMockUser
    void delete_missing_404() throws Exception {
        mockMvc.perform(delete("/api/analysis/{id}", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE invalid id format returns 400 (type mismatch)")
    @WithMockUser
    void delete_invalid_id_format() throws Exception {
        mockMvc.perform(delete("/api/analysis/{id}", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Method not allowed returns 405 on PUT collection")
    @WithMockUser
    void method_not_allowed() throws Exception {
        mockMvc.perform(put("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("E2E workflow: POST -> PUT -> GET -> DELETE")
    @WithMockUser
    void e2e_workflow() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("comment", "workflow");
        payload.put("lane", 5);
        payload.put("s_id", "S200");
        payload.put("s_stamp", "2024-12-10T10:00:00");

        String res = mockMvc.perform(post("/api/analysis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Analysis created = objectMapper.readValue(res, Analysis.class);
        Long id = created.getId();

        payload.put("comment", "updated workflow");
        mockMvc.perform(put("/api/analysis/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("updated workflow"));

        mockMvc.perform(get("/api/analysis/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("updated workflow"));

        mockMvc.perform(delete("/api/analysis/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analysis/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create many then page through them")
    @WithMockUser
    void create_many_and_page() throws Exception {
        for (int i = 0; i < 10; i++) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("comment", "item" + i);
            payload.put("s_id", "S" + (300 + i));
            payload.put("s_stamp", "2024-12-20T10:00:00");

            mockMvc.perform(post("/api/analysis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(payload)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/analysis").param("size", "5").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }
}
