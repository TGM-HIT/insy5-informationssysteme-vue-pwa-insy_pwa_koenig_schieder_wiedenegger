package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.BoxPosController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.BoxPos;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.BoxPosId;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.BoxPosRepository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.BoxRepository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = BoxPosController.class)
class BoxPosControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BoxPosRepository repository;

    @MockBean
    BoxRepository boxRepository;

    @MockBean
    SampleRepository sampleRepository;

    @Test
    @DisplayName("GET /api/boxpos returns 200 and empty page")
    void list_ok() throws Exception {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(Collections.emptyList(), inv.getArgument(0), 0));

        mockMvc.perform(get("/api/boxpos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/boxpos supports paging params")
    void list_paging_ok() throws Exception {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(java.util.List.of(new BoxPos()), inv.getArgument(0), 5));
        mockMvc.perform(get("/api/boxpos").param("size", "1").param("page", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.number").value(4))
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    @DisplayName("GET /api/boxpos/{bId}/{bposId} returns 404 when not found")
    void get_not_found() throws Exception {
        when(repository.findById(new BoxPosId(1, "B001"))).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/boxpos/{bId}/{bposId}", "B001", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/boxpos/{bId}/{bposId} returns 200 when exists")
    void get_ok() throws Exception {
        BoxPosId id = new BoxPosId(10, "B010");
        BoxPos bp = new BoxPos();
        bp.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(bp));
        mockMvc.perform(get("/api/boxpos/{bId}/{bposId}", "B010", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b_id").value("B010"))
                .andExpect(jsonPath("$.bpos_id").value(10));
    }

    @Test
    @DisplayName("POST /api/boxpos without composite id returns 400")
    void post_missing_id_bad_request() throws Exception {
        String payload = "{}";
        mockMvc.perform(post("/api/boxpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/boxpos creates and returns 201 with Location")
    void post_ok() throws Exception {
        BoxPosId id = new BoxPosId(1, "B001");
        when(repository.existsById(id)).thenReturn(false);
        when(boxRepository.existsById("B001")).thenReturn(true);
        when(repository.save(any(BoxPos.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/api/boxpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b_id\":\"B001\",\"bpos_id\":1}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/boxpos/B001/1")))
                .andExpect(jsonPath("$.b_id").value("B001"))
                .andExpect(jsonPath("$.bpos_id").value(1));
    }

    @Test
    @DisplayName("POST /api/boxpos duplicate -> 409")
    void post_conflict() throws Exception {
        BoxPosId id = new BoxPosId(1, "B001");
        when(repository.existsById(id)).thenReturn(true);
        mockMvc.perform(post("/api/boxpos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b_id\":\"B001\",\"bpos_id\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/boxpos/{bId}/{bposId} returns 404 when not exists")
    void put_not_found() throws Exception {
        BoxPosId id = new BoxPosId(2, "B123");
        when(repository.existsById(id)).thenReturn(false);
        mockMvc.perform(put("/api/boxpos/{bId}/{bposId}", "B123", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/boxpos/{bId}/{bposId} updates and returns 200")
    void put_ok() throws Exception {
        BoxPosId id = new BoxPosId(5, "B005");
        when(repository.existsById(id)).thenReturn(true);
        when(repository.save(any(BoxPos.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(put("/api/boxpos/{bId}/{bposId}", "B005", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"some\":\"field\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b_id").value("B005"))
                .andExpect(jsonPath("$.bpos_id").value(5));
    }

    @Test
    @DisplayName("DELETE /api/boxpos/{bId}/{bposId} returns 404 when not exists")
    void delete_not_found() throws Exception {
        BoxPosId id = new BoxPosId(3, "B999");
        when(repository.existsById(id)).thenReturn(false);
        mockMvc.perform(delete("/api/boxpos/{bId}/{bposId}", "B999", 3))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/boxpos/{bId}/{bposId} returns 200 when deleted")
    void delete_ok() throws Exception {
        BoxPosId id = new BoxPosId(7, "B007");
        when(repository.existsById(id)).thenReturn(true);
        mockMvc.perform(delete("/api/boxpos/{bId}/{bposId}", "B007", 7))
                .andExpect(status().isOk());
    }
}
