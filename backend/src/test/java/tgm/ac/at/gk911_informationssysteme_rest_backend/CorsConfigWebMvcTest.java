package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.config.CorsConfig;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.AnalysisController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.AnalysisRepository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = AnalysisController.class)
@Import(CorsConfig.class)
class CorsConfigWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AnalysisRepository repository;

    @MockBean
    SampleRepository sampleRepository;

    @Test
    @DisplayName("CORS preflight on /api/** returns configured headers")
    void cors_preflight_ok() throws Exception {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(java.util.Collections.emptyList(), inv.getArgument(0), 0));
        mockMvc.perform(options("/api/analysis")
                        .header("Origin", "http://example.com")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "X-Test"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://example.com"))
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().exists("Access-Control-Allow-Headers"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    @DisplayName("Actual GET with Origin includes ACAO and exposed headers, without credentials")
    void cors_actual_get_ok() throws Exception {
        when(repository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(java.util.Collections.emptyList(), inv.getArgument(0), 0));
        mockMvc.perform(get("/api/analysis")
                        .header("Origin", "http://example.org")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://example.org"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    @DisplayName("CORS not applied outside /api/** mapping")
    void cors_not_applied_for_other_paths() throws Exception {
        mockMvc.perform(options("/not-api")
                        .header("Origin", "http://x.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Methods"));
    }
}
