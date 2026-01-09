package tgm.ac.at.gk911_informationssysteme_rest_backend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tgm.ac.at.gk911_informationssysteme_rest_backend.controller.TestController;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = TestController.class)
class TestControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EntityManager entityManager;

    @MockBean(name = "sampleRepository")
    SampleRepository sampleRepository;

    @MockBean(name = "boxRepository")
    BoxRepository boxRepository;

    @MockBean(name = "boxPosRepository")
    BoxPosRepository boxPosRepository;

    @MockBean(name = "logRepository")
    LogRepository logRepository;

    @MockBean(name = "analysisRepository")
    AnalysisRepository analysisRepository;

    // Health endpoint tests (bereits vorhanden)
    @Test
    @DisplayName("GET /api/test/health returns 200 with status ok")
    void health_returns_ok() throws Exception {
        mockMvc.perform(get("/api/test/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.message").value("Test controller is active"));
    }

    @Test
    @DisplayName("GET /api/test/health returns JSON content type")
    void health_json_content_type() throws Exception {
        mockMvc.perform(get("/api/test/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @DisplayName("GET /api/test/health contains required fields")
    void health_contains_required_fields() throws Exception {
        mockMvc.perform(get("/api/test/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists());
    }

    // Cleanup endpoint tests (NEU)
    @Test
    @DisplayName("DELETE /api/test/cleanup successfully deletes test data")
    void cleanup_success() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(5, 3, 2, 4, 6); // Log, Analysis, BoxPos, Sample, Box

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Test data cleaned successfully"))
                .andExpect(jsonPath("$.totalDeleted").value(20))
                .andExpect(jsonPath("$.logsDeleted").value(5))
                .andExpect(jsonPath("$.analysesDeleted").value(3))
                .andExpect(jsonPath("$.boxPosDeleted").value(2))
                .andExpect(jsonPath("$.samplesDeleted").value(4))
                .andExpect(jsonPath("$.boxesDeleted").value(6));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with no data returns zero counts")
    void cleanup_no_data() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0, 0, 0, 0, 0);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.totalDeleted").value(0))
                .andExpect(jsonPath("$.logsDeleted").value(0))
                .andExpect(jsonPath("$.analysesDeleted").value(0))
                .andExpect(jsonPath("$.boxPosDeleted").value(0))
                .andExpect(jsonPath("$.samplesDeleted").value(0))
                .andExpect(jsonPath("$.boxesDeleted").value(0));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup handles exception returns 500")
    void cleanup_exception() throws Exception {
        when(entityManager.createQuery(anyString())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Database error"))
                .andExpect(jsonPath("$.errorType").value("RuntimeException"));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with null entityManager handles gracefully")
    void cleanup_null_entity_manager() throws Exception {
        // EntityManager ist gemockt aber gibt null zurück
        when(entityManager.createQuery(anyString())).thenReturn(null);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup deletes logs first")
    void cleanup_logs_first() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(10, 0, 0, 0, 0);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logsDeleted").value(10));

        verify(entityManager, times(5)).createQuery(anyString());
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with only analysis data")
    void cleanup_only_analysis() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0, 15, 0, 0, 0);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysesDeleted").value(15))
                .andExpect(jsonPath("$.totalDeleted").value(15));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with only boxpos data")
    void cleanup_only_boxpos() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0, 0, 8, 0, 0);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxPosDeleted").value(8))
                .andExpect(jsonPath("$.totalDeleted").value(8));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with only sample data")
    void cleanup_only_samples() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0, 0, 0, 12, 0);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.samplesDeleted").value(12))
                .andExpect(jsonPath("$.totalDeleted").value(12));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with only box data")
    void cleanup_only_boxes() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(0, 0, 0, 0, 7);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boxesDeleted").value(7))
                .andExpect(jsonPath("$.totalDeleted").value(7));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with large numbers")
    void cleanup_large_numbers() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(100, 200, 150, 250, 300);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDeleted").value(1000));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup with NullPointerException")
    void cleanup_null_pointer_exception() throws Exception {
        when(entityManager.createQuery(anyString())).thenThrow(new NullPointerException("Null entity"));

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errorType").value("NullPointerException"));
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup prints stack trace on error")
    void cleanup_prints_stack_trace() throws Exception {
        when(entityManager.createQuery(anyString())).thenThrow(new IllegalStateException("Test error"));

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Test error"));
    }

    @Test
    @DisplayName("GET /api/test/health is idempotent")
    void health_idempotent() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/test/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
        }
    }

    @Test
    @DisplayName("DELETE /api/test/cleanup returns correct structure")
    void cleanup_correct_structure() throws Exception {
        Query mockQuery = mock(Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(mockQuery);
        when(mockQuery.executeUpdate()).thenReturn(1, 2, 3, 4, 5);

        mockMvc.perform(delete("/api/test/cleanup"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.totalDeleted").exists())
                .andExpect(jsonPath("$.logsDeleted").exists())
                .andExpect(jsonPath("$.analysesDeleted").exists())
                .andExpect(jsonPath("$.boxPosDeleted").exists())
                .andExpect(jsonPath("$.samplesDeleted").exists())
                .andExpect(jsonPath("$.boxesDeleted").exists());
    }
}
