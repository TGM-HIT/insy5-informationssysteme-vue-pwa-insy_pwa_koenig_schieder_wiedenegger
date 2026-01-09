package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.SampleController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Sample;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.SampleId;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = SampleController.class)
class SampleIdControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SampleRepository repository;

    // ===== GET LIST TESTS =====
    @Test
    @DisplayName("GET /api/sample returns list with multiple samples")
    void list_with_multiple_samples() throws Exception {
        LocalDateTime ts1 = LocalDateTime.parse("2024-01-01T10:00:00");
        LocalDateTime ts2 = LocalDateTime.parse("2024-02-01T12:00:00");

        Sample s1 = new Sample();
        s1.setId(new SampleId("S1", ts1));
        s1.setName("Sample One");

        Sample s2 = new Sample();
        s2.setId(new SampleId("S2", ts2));
        s2.setName("Sample Two");

        when(repository.findAll()).thenReturn(List.of(s1, s2));

        mockMvc.perform(get("/api/sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].s_id").value("S1"))
                .andExpect(jsonPath("$[1].s_id").value("S2"));
    }

    @Test
    @DisplayName("GET /api/sample with empty list returns empty array")
    void list_empty_returns_empty_array() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/sample returns JSON content type")
    void list_json_content_type() throws Exception {
        when(repository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/sample"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // ===== GET BY ID TESTS =====
    @Test
    @DisplayName("GET /api/sample/{sId}/{sStamp} with valid id returns sample")
    void get_with_valid_id() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-01-01T10:00:00");
        SampleId id = new SampleId("S1", ts);

        Sample sample = new Sample();
        sample.setId(id);
        sample.setName("Test Sample");

        when(repository.findById(id)).thenReturn(Optional.of(sample));

        mockMvc.perform(get("/api/sample/{sId}/{sStamp}", "S1", "2024-01-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("S1"))
                .andExpect(jsonPath("$.name").value("Test Sample"));
    }

    @Test
    @DisplayName("GET /api/sample/{sId}/{sStamp} returns 404 when not found")
    void get_not_found() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-01-01T10:00:00");
        when(repository.findById(new SampleId("NONE", ts))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/sample/{sId}/{sStamp}", "NONE", "2024-01-01T10:00:00"))
                .andExpect(status().isNotFound());
    }

    // ===== POST TESTS =====
    @Test
    @DisplayName("POST /api/sample with valid data succeeds")
    void post_valid_data() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-05-05T05:05:05");
        SampleId id = new SampleId("S5", ts);

        when(repository.existsById(id)).thenReturn(false);
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S5\",\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("S5"));
    }

    @Test
    @DisplayName("POST /api/sample with 13 char s_id length succeeds")
    void post_valid_13_char_sId() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-05-05T05:05:05");
        SampleId id = new SampleId("S123456789012", ts);

        when(repository.existsById(id)).thenReturn(false);
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S123456789012\",\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/sample with 14+ char s_id returns 400")
    void post_sId_too_long() throws Exception {
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S12345678901234\",\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample with null s_id returns 400")
    void post_null_sId() throws Exception {
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":null,\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample with null s_stamp returns 400")
    void post_null_sStamp() throws Exception {
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S1\",\"s_stamp\":null}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample with null id returns 400")
    void post_null_id() throws Exception {
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample with duplicate returns 409")
    void post_duplicate_conflict() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-06-06T06:06:06");
        SampleId id = new SampleId("DUP", ts);

        when(repository.existsById(id)).thenReturn(true);

        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"DUP\",\"s_stamp\":\"2024-06-06T06:06:06\"}}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/sample with minimum fields")
    void post_minimum_fields() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-10-10T10:10:10");
        SampleId id = new SampleId("MIN", ts);

        when(repository.existsById(id)).thenReturn(false);
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"MIN\",\"s_stamp\":\"2024-10-10T10:10:10\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("MIN"));
    }

    // ===== PUT TESTS =====
    @Test
    @DisplayName("PUT /api/sample updates all fields correctly")
    void put_updates_all_fields() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-07-07T07:07:07");
        SampleId id = new SampleId("S7", ts);

        Sample existing = new Sample();
        existing.setId(id);
        existing.setName("Old Name");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/sample/{sId}/{sStamp}", "S7", "2024-07-07T07:07:07")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\",\"comment\":\"New Comment\",\"quantity\":42}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.comment").value("New Comment"))
                .andExpect(jsonPath("$.quantity").value(42));
    }

    @Test
    @DisplayName("PUT /api/sample for non-existing returns 404")
    void put_non_existing() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-09-09T09:09:09");
        SampleId id = new SampleId("NONE", ts);

        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/sample/{sId}/{sStamp}", "NONE", "2024-09-09T09:09:09")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/sample updates name field")
    void put_updates_name() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-08-08T08:08:08");
        SampleId id = new SampleId("UPD", ts);

        Sample existing = new Sample();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/sample/{sId}/{sStamp}", "UPD", "2024-08-08T08:08:08")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        verify(repository).save(any(Sample.class));
    }

    // ===== DELETE TESTS =====
    @Test
    @DisplayName("DELETE /api/sample with valid id returns 200")
    void delete_valid_id() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-08-08T08:08:08");
        SampleId id = new SampleId("DEL", ts);

        Sample existing = new Sample();
        existing.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/sample/{sId}/{sStamp}", "DEL", "2024-08-08T08:08:08"))
                .andExpect(status().isOk());

        verify(repository).delete(any(Sample.class));
    }

    @Test
    @DisplayName("DELETE /api/sample for non-existing returns 404")
    void delete_non_existing() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-11-11T11:11:11");
        SampleId id = new SampleId("NONE", ts);

        when(repository.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/sample/{sId}/{sStamp}", "NONE", "2024-11-11T11:11:11"))
                .andExpect(status().isNotFound());

        verify(repository, never()).delete(any(Sample.class));
    }
}
