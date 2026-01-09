package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.BoxController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Box;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.BoxRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = BoxController.class)
class BoxControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BoxRepository repository;

    @Test
    @DisplayName("GET /api/box returns 200 and empty list")
    void list_ok() throws Exception {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/box"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/box/{id} returns 404 when not found")
    void get_not_found() throws Exception {
        when(repository.findById("X001")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/box/{id}", "X001"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/box/{id} returns 200 with body when exists")
    void get_ok() throws Exception {
        Box b = new Box();
        b.setId("B111");
        b.setName("Box 111");
        when(repository.findById("B111")).thenReturn(Optional.of(b));
        mockMvc.perform(get("/api/box/{id}", "B111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b_id").value("B111"))
                .andExpect(jsonPath("$.name").value("Box 111"));
    }

    @Test
    @DisplayName("POST /api/box creates and returns 200 with body")
    void post_ok() throws Exception {
        Box input = new Box();
        input.setId("B001");
        input.setName("Alpha");
        when(repository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/api/box")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"b_id\":\"B001\",\"name\":\"Alpha\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b_id").value("B001"))
                .andExpect(jsonPath("$.name").value("Alpha"));
    }

    @Test
    @DisplayName("PUT /api/box/{id} returns 404 when not exists")
    void put_not_found() throws Exception {
        when(repository.findById("B123")).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/box/{id}", "B123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/box/{id} updates and returns 200")
    void put_ok() throws Exception {
        Box existing = new Box();
        existing.setId("B010");
        existing.setName("Old Name");
        when(repository.findById("B010")).thenReturn(Optional.of(existing));
        when(repository.save(any(Box.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(put("/api/box/{id}", "B010")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New Name\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.b_id").value("B010"))
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    @DisplayName("DELETE /api/box/{id} returns 404 when not exists")
    void delete_not_found() throws Exception {
        when(repository.findById("B999")).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/box/{id}", "B999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/box/{id} returns 200 when deleted")
    void delete_ok() throws Exception {
        Box existing = new Box();
        existing.setId("B777");
        when(repository.findById("B777")).thenReturn(Optional.of(existing));
        mockMvc.perform(delete("/api/box/{id}", "B777"))
                .andExpect(status().isOk());
    }
}
