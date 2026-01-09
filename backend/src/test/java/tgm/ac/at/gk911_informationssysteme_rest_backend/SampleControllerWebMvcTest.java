package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.config.AuthController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.config.SecurityConfig;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.SampleController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Sample;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.SampleId;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = SampleController.class)
@Import(SecurityConfig.class)
class SampleControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SampleRepository repository;

    @MockBean
    AuthController authController;

    @Test
    @DisplayName("GET /api/sample returns 200 and empty list")
    void list_ok() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/sample"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/sample/{sId}/{sStamp} returns 404 when not found")
    void get_not_found() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-01-01T10:00:00");
        when(repository.findById(new SampleId("S1", ts))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/sample/{sId}/{sStamp}", "S1", "2024-01-01T10:00:00"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/sample/{sId}/{sStamp} returns 200 when exists")
    void get_ok() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-01-01T10:00:00");
        Sample s = new Sample();
        s.setId(new SampleId("S1", ts));
        when(repository.findById(new SampleId("S1", ts))).thenReturn(Optional.of(s));
        mockMvc.perform(get("/api/sample/{sId}/{sStamp}", "S1", "2024-01-01T10:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("S1"))
                .andExpect(jsonPath("$.s_stamp").value("2024-01-01T10:00:00"));
    }

    @Test
    @DisplayName("POST /api/sample without composite id returns 400")
    void post_missing_id_bad_request() throws Exception {
        String payload = "{}";
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample with s_id too long returns 400")
    void post_sId_too_long() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-05-05T05:05:05");
        SampleId id = new SampleId("S5678901234567", ts);
        when(repository.existsById(id)).thenReturn(false);
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S5678901234567\",\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/sample creates and returns 200 with body")
    void post_ok() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-05-05T05:05:05");
        SampleId id = new SampleId("S5", ts);
        when(repository.existsById(id)).thenReturn(false);
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S5\",\"s_stamp\":\"2024-05-05T05:05:05\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("S5"))
                .andExpect(jsonPath("$.s_stamp").value("2024-05-05T05:05:05"));
    }

    @Test
    @DisplayName("POST /api/sample returns 409 on duplicate id")
    void post_conflict_on_duplicate() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-06-06T06:06:06");
        SampleId id = new SampleId("S6", ts);
        when(repository.existsById(id)).thenReturn(true);
        mockMvc.perform(post("/api/sample")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":{\"s_id\":\"S6\",\"s_stamp\":\"2024-06-06T06:06:06\"}}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/sample/{sId}/{sStamp} returns 404 when not exists")
    void put_not_found() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-02-02T12:00:00");
        SampleId id = new SampleId("S2", ts);
        when(repository.findById(id)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/sample/{sId}/{sStamp}", "S2", "2024-02-02T12:00:00")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/sample/{sId}/{sStamp} updates and returns 200")
    void put_ok() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-07-07T07:07:07");
        SampleId id = new SampleId("S7", ts);
        Sample existing = new Sample();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Sample.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(put("/api/sample/{sId}/{sStamp}", "S7", "2024-07-07T07:07:07")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.s_id").value("S7"))
                .andExpect(jsonPath("$.s_stamp").value("2024-07-07T07:07:07"));
    }

    @Test
    @DisplayName("DELETE /api/sample/{sId}/{sStamp} returns 404 when not exists")
    void delete_not_found() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-03-03T08:00:00");
        SampleId id = new SampleId("S3", ts);
        when(repository.findById(id)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/sample/{sId}/{sStamp}", "S3", "2024-03-03T08:00:00"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/sample/{sId}/{sStamp} returns 200 when deleted")
    void delete_ok() throws Exception {
        LocalDateTime ts = LocalDateTime.parse("2024-08-08T08:08:08");
        SampleId id = new SampleId("S8", ts);
        Sample existing = new Sample();
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        mockMvc.perform(delete("/api/sample/{sId}/{sStamp}", "S8", "2024-08-08T08:08:08"))
                .andExpect(status().isOk());
    }
}
