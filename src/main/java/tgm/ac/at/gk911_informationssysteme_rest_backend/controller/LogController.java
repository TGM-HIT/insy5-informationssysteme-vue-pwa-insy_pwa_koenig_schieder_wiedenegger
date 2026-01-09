package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Log;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.LogRepository;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/log")  // ✓ Geändert von /api/log zu /api/logs
@CrossOrigin(origins = "*")
public class LogController {

    @Autowired
    private LogRepository logRepository;

    @GetMapping
    public ResponseEntity<List<Log>> getAll() {
        return ResponseEntity.ok(logRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Log> getLogById(@PathVariable Long id) {
        return logRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Log> createLog(@Valid @RequestBody Log log) {
        if (log.getDateCreated() == null) {
            log.setDateCreated(Instant.now());
        }
        Log savedLog = logRepository.save(log);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Log> updateLog(@PathVariable Long id, @Valid @RequestBody Log logDetails) {
        return logRepository.findById(id)
                .map(existingLog -> {
                    existingLog.setLevel(logDetails.getLevel());
                    existingLog.setInfo(logDetails.getInfo());
                    existingLog.setDateExported(logDetails.getDateExported());
                    // Update auch andere Felder falls nötig
                    if (logDetails.getSId() != null) existingLog.setSId(logDetails.getSId());
                    if (logDetails.getSStamp() != null) existingLog.setSStamp(logDetails.getSStamp());
                    if (logDetails.getAId() != null) existingLog.setAId(logDetails.getAId());

                    return ResponseEntity.ok(logRepository.save(existingLog));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        if (!logRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        logRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
