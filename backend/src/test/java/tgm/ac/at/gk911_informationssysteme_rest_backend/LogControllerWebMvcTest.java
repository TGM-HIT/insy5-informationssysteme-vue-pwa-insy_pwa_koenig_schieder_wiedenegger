package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.LogController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Log;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.LogRepository;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = LogController.class)
class LogControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LogRepository logRepository;

    @Test
    @DisplayName("GET /api/log returns 200 and empty list")
    void list_ok() throws Exception {
        when(logRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/log"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("GET /api/log/{id} returns 404 when not found")
    void get_not_found() throws Exception {
        when(logRepository.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/log/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/log/{id} returns 200 when exists")
    void get_ok() throws Exception {
        Log l = new Log();
        l.setId(10L);
        l.setLevel("INFO");
        when(logRepository.findById(10L)).thenReturn(Optional.of(l));
        mockMvc.perform(get("/api/log/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.level").value("INFO"));
    }

    @Test
    @DisplayName("POST /api/log creates 201 and sets dateCreated when missing")
    void post_ok_sets_date_created() throws Exception {
        when(logRepository.save(any(Log.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(post("/api/log")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"WARN\",\"info\":\"x\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.level").value("WARN"))
                .andExpect(jsonPath("$.dateCreated").exists());
    }

    @Test
    @DisplayName("PUT /api/log/{id} returns 404 when not exists")
    void put_not_found() throws Exception {
        when(logRepository.findById(5L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/log/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/log/{id} updates and returns 200")
    void put_ok() throws Exception {
        Log existing = new Log();
        existing.setId(7L);
        existing.setLevel("INFO");
        when(logRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(logRepository.save(any(Log.class))).thenAnswer(inv -> inv.getArgument(0));
        mockMvc.perform(put("/api/log/{id}", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"level\":\"ERROR\",\"info\":\"changed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("ERROR"))
                .andExpect(jsonPath("$.info").value("changed"));
    }

    @Test
    @DisplayName("DELETE /api/log/{id} returns 404 when not exists")
    void delete_not_found() throws Exception {
        when(logRepository.existsById(9L)).thenReturn(false);
        mockMvc.perform(delete("/api/log/{id}", 9L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/log/{id} returns 204 when deleted")
    void delete_ok() throws Exception {
        when(logRepository.existsById(11L)).thenReturn(true);
        mockMvc.perform(delete("/api/log/{id}", 11L))
                .andExpect(status().isNoContent());
    }
}
