package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.*;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.*;

import java.net.URI;

@RestController
@RequestMapping(value = "/api/analysis", produces = "application/json")
@Tag(name = "Analysis", description = "CRUD endpoints for venlab.analysis")
public class AnalysisController {

    private final AnalysisRepository repository;
    private final SampleRepository sampleRepository;

    public AnalysisController(AnalysisRepository repository, SampleRepository sampleRepository) {
        this.repository = repository;
        this.sampleRepository = sampleRepository;
    }

    @GetMapping
    @Operation(summary = "List analyses", description = "Returns a paged list of analyses")
    public Page<Analysis> list(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get analysis by ID")
    public ResponseEntity<Analysis> get(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok) // 200 OK mit Analysis im Body
                .orElseGet(() -> ResponseEntity.notFound().build()); // 404 ohne Body
    }


    @PostMapping
    @Operation(summary = "Create analysis")
    public ResponseEntity<Analysis> create(@RequestBody @Validated Analysis analysis) {
        if (analysis.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New analysis must not have an id");
        }
        // Check foreign key constraint
        if (analysis.getSId() != null && analysis.getSStamp() != null) {
            SampleId sampleId = new SampleId(analysis.getSId(), analysis.getSStamp());
            if (!sampleRepository.existsById(sampleId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Foreign key violation: Sample with id (" + analysis.getSId() + ", " + analysis.getSStamp() + ") does not exist.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "s_id and s_stamp are required to reference a sample");
        }

        Analysis saved = repository.save(analysis);
        return ResponseEntity.created(URI.create("/api/analysis/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update analysis by ID")
    public Analysis update(@PathVariable Long id, @RequestBody @Validated Analysis analysis) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found");
        }
        analysis.setId(id);
        return repository.save(analysis);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete analysis by ID")
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found");
        }
        repository.deleteById(id);
    }
}
