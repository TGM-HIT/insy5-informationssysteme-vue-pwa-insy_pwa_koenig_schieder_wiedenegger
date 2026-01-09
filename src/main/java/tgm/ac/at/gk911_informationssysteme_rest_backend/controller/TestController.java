package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = false)
    private SampleRepository sampleRepository;

    @Autowired(required = false)
    private BoxRepository boxRepository;

    @Autowired(required = false)
    private BoxPosRepository boxPosRepository;  // HINZUGEFÜGT

    @Autowired(required = false)
    private LogRepository logRepository;

    @Autowired(required = false)
    private AnalysisRepository analysisRepository;

    /**
     * Löscht alle Test-Daten (Einträge die mit TEST, CYPRESS, READ, UPDATE, DELETE, etc. beginnen)
     */
    @DeleteMapping("/cleanup")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupTestData() {
        Map<String, Object> result = new HashMap<>();
        int totalDeleted = 0;

        try {
            // WICHTIG: Reihenfolge wegen Foreign Keys!
            // 1. Zuerst Logs (haben Foreign Keys zu Sample und Analysis)
            if (logRepository != null) {
                int logCount = entityManager.createQuery(
                        "DELETE FROM Log l WHERE " +
                                "l.sId LIKE 'TEST%' OR " +
                                "l.sId LIKE 'CYPRESS%' OR " +
                                "l.sId LIKE 'READ%' OR " +
                                "l.sId LIKE 'UPDT%' OR " +
                                "l.sId LIKE 'UPDS%' OR " +
                                "l.sId LIKE 'DELE%' OR " +
                                "l.sId LIKE 'DUPL%' OR " +
                                "l.sId LIKE 'ERRS%' OR " +
                                "l.sId LIKE 'T0%' OR " +  // Für T001, T002, etc.
                                "l.sId LIKE 'SAMP_%' OR " +
                                "l.sId LIKE 'STAMP%' OR " +
                                "l.sId LIKE 'ERROR%' OR " +
                                "l.info LIKE '%Cypress%' OR " +
                                "l.info LIKE '%Test%' OR " +
                                "l.info LIKE '%test%'"
                ).executeUpdate();
                result.put("logsDeleted", logCount);
                totalDeleted += logCount;
            }

            // 2. Analysis Cleanup
            if (analysisRepository != null) {
                int analysisCount = entityManager.createQuery(
                        "DELETE FROM Analysis a WHERE " +
                                "a.sId LIKE 'TEST%' OR " +
                                "a.sId LIKE 'CYPRESS%' OR " +
                                "a.sId LIKE 'READ%' OR " +
                                "a.sId LIKE 'UPDT%' OR " +
                                "a.sId LIKE 'UPDS%' OR " +
                                "a.sId LIKE 'DELE%' OR " +
                                "a.sId LIKE 'DUPL%' OR " +
                                "a.sId LIKE 'ERRS%' OR " +
                                "a.sId LIKE 'T0%' OR " +
                                "a.sId LIKE 'SAMP_%' OR " +
                                "a.sId LIKE 'ERROR%' OR " +
                                "a.comment LIKE '%Cypress%' OR " +
                                "a.comment LIKE '%Test%' OR " +
                                "a.comment LIKE '%test%'"
                ).executeUpdate();
                result.put("analysesDeleted", analysisCount);
                totalDeleted += analysisCount;
            }

            // 3. BoxPos Cleanup - MUSS VOR Box und Sample kommen!
            if (boxPosRepository != null) {
                int boxPosCount = entityManager.createQuery(
                        "DELETE FROM BoxPos bp WHERE " +
                                "bp.id.bId LIKE 'TEST%' OR " +
                                "bp.id.bId LIKE 'TSMP%' OR " +
                                "bp.id.bId LIKE 'READ%' OR " +
                                "bp.id.bId LIKE 'UPDT%' OR " +
                                "bp.id.bId LIKE 'UPDS%' OR " +
                                "bp.id.bId LIKE 'DELE%' OR " +
                                "bp.id.bId LIKE 'DUPL%' OR " +
                                "bp.id.bId LIKE 'ERRS%' OR " +
                                "bp.id.bId LIKE 'T0%' OR " +  // Für T001, T002, etc.
                                "bp.sampleId LIKE 'SAMP_%' OR " +
                                "bp.sampleId LIKE 'TEST%'"
                ).executeUpdate();
                result.put("boxPosDeleted", boxPosCount);
                totalDeleted += boxPosCount;
            }

            // 4. Sample Cleanup
            if (sampleRepository != null) {
                int sampleCount = entityManager.createQuery(
                        "DELETE FROM Sample s WHERE " +
                                "s.id.sId LIKE 'TEST%' OR " +
                                "s.id.sId LIKE 'CYPRESS%' OR " +
                                "s.id.sId LIKE 'READ%' OR " +
                                "s.id.sId LIKE 'UPDT%' OR " +
                                "s.id.sId LIKE 'UPDS%' OR " +
                                "s.id.sId LIKE 'DELE%' OR " +
                                "s.id.sId LIKE 'DUPL%' OR " +
                                "s.id.sId LIKE 'ERRS%' OR " +
                                "s.id.sId LIKE 'SAMP_%' OR " +  // Für SAMP_T001, etc.
                                "s.id.sId LIKE 'STAMP%' OR " +
                                "s.id.sId LIKE 'ERROR%' OR " +
                                "s.name LIKE '%Cypress%' OR " +
                                "s.name LIKE '%Test%' OR " +
                                "s.comment LIKE '%Cypress%' OR " +
                                "s.comment LIKE '%test%'"
                ).executeUpdate();
                result.put("samplesDeleted", sampleCount);
                totalDeleted += sampleCount;
            }

            // 5. Box Cleanup - MUSS NACH BoxPos kommen!
            if (boxRepository != null) {
                int boxCount = entityManager.createQuery(
                        "DELETE FROM Box b WHERE " +
                                "b.id LIKE 'TEST%' OR " +
                                "b.id LIKE 'TSMP%' OR " +
                                "b.id LIKE 'READ%' OR " +
                                "b.id LIKE 'UPDT%' OR " +
                                "b.id LIKE 'UPDS%' OR " +
                                "b.id LIKE 'DELE%' OR " +
                                "b.id LIKE 'DUPL%' OR " +
                                "b.id LIKE 'ERRS%' OR " +
                                "b.id LIKE 'T0%' OR " +  // Für T001, T002, etc.
                                "b.id LIKE 'BOX_TEST%' OR " +
                                "b.id LIKE 'BOX_READ%' OR " +
                                "b.id LIKE 'BOX_UPDATE%' OR " +
                                "b.id LIKE 'BOX_DELETE%' OR " +
                                "b.id LIKE 'BOX_MINIMAL%' OR " +
                                "b.id LIKE 'CYPRESS%' OR " +
                                "b.name LIKE '%Cypress%' OR " +
                                "b.name LIKE '%Test%' OR " +
                                "b.comment LIKE '%Cypress%' OR " +
                                "b.comment LIKE '%test%'"
                ).executeUpdate();
                result.put("boxesDeleted", boxCount);
                totalDeleted += boxCount;
            }

            result.put("totalDeleted", totalDeleted);
            result.put("status", "success");
            result.put("message", "Test data cleaned successfully");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace(); // Für Debugging
            result.put("status", "error");
            result.put("message", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * Health Check Endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Test controller is active");
        return ResponseEntity.ok(response);
    }
}
