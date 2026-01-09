package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.*;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.*;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping(value = "/api/boxpos", produces = "application/json")
@Tag(name = "BoxPos", description = "CRUD endpoints for venlab.boxpos")
public class BoxPosController {

    private final BoxPosRepository repository;
    private final SampleRepository sampleRepository;
    private final BoxRepository boxRepository;

    public BoxPosController(BoxPosRepository repository, SampleRepository sampleRepository, BoxRepository boxRepository) {
        this.repository = repository;
        this.sampleRepository = sampleRepository;
        this.boxRepository = boxRepository;
    }

    @GetMapping
    @Operation(summary = "List box positions")
    public Page<BoxPos> list(
            @RequestParam(name = "bId", required = false) String bId,
            @PageableDefault(size = 20) Pageable pageable) {
        if (bId != null && !bId.isBlank()) {
            return repository.findById_BId(bId, pageable);
        }
        return repository.findAll(pageable);
    }

    @GetMapping("/{bId}/{bposId}")
    @Operation(summary = "Get box position by composite id")
    public BoxPos get(
            @PathVariable String bId,
            @PathVariable Integer bposId
    ) {
        return repository.findById(new BoxPosId(bposId, bId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BoxPos not found"));
    }

    @PostMapping
    @Operation(summary = "Create box position")
    public ResponseEntity<BoxPos> create(@RequestBody BoxPos boxPos) {
        if (boxPos.getId() == null || boxPos.getId().getBposId() == null || boxPos.getId().getbId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Composite id (b_id, bpos_id) is required");
        }

        BoxPosId id = boxPos.getId();
        if (repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "BoxPos with id (" + id.getbId() + ", " + id.getBposId() + ") already exists");
        }

        // Check foreign key constraints
        if (!boxRepository.existsById(id.getbId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Foreign key violation: Box with id " + id.getbId() + " does not exist.");
        }
        if (boxPos.getSampleId() != null && boxPos.getSampleStamp() != null) {
            SampleId sampleId = new SampleId(boxPos.getSampleId(), boxPos.getSampleStamp().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
            if (!sampleRepository.existsById(sampleId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Foreign key violation: Sample with id (" + boxPos.getSampleId() + ", " + boxPos.getSampleStamp() + ") does not exist.");
            }
        }


        BoxPos saved = repository.save(boxPos);
        return ResponseEntity.created(URI.create("/api/boxpos/" + id.getbId() + "/" + id.getBposId())).body(saved);
    }

    @PutMapping("/{bId}/{bposId}")
    @Operation(summary = "Update (replace) box position by composite id")
    public BoxPos update(
            @PathVariable String bId,
            @PathVariable Integer bposId,
            @RequestBody BoxPos boxPos
    ) {
        BoxPosId id = new BoxPosId(bposId, bId);
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BoxPos not found");
        }
        boxPos.setId(id);
        return repository.save(boxPos);
    }

    @DeleteMapping("/{bId}/{bposId}")
    @Operation(summary = "Delete box position by composite id")
    public void delete(
            @PathVariable String bId,
            @PathVariable Integer bposId
    ) {
        BoxPosId id = new BoxPosId(bposId, bId);
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "BoxPos not found");
        }
        repository.deleteById(id);
    }
}