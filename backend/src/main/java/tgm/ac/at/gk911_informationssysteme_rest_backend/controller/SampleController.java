package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Sample;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.SampleId;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.SampleRepository;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sample")
@CrossOrigin(origins = "*")
public class SampleController {

    @Autowired
    private SampleRepository repository;

    @GetMapping
    public List<Sample> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{sId}/{sStamp}")
    public ResponseEntity<Sample> getById(
            @PathVariable String sId,
            @PathVariable String sStamp) {

        LocalDateTime dateTime = LocalDateTime.parse(sStamp);

        return repository.findById(new SampleId(sId, dateTime))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Sample create(@RequestBody Sample sample) {
        if (sample.getId() == null || sample.getId().getsId() == null || sample.getId().getsStamp() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Composite id (s_id, s_stamp) is required");
        }
        if (sample.getId().getsId().length() > 13) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "s_id must not be longer than 13 characters");
        }
        if (repository.existsById(sample.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Sample with id (" + sample.getId().getsId() + ", " + sample.getId().getsStamp() + ") already exists");
        }
        return repository.save(sample);
    }

    @PutMapping("/{sId}/{sStamp}")
    public ResponseEntity<Sample> update(
            @PathVariable String sId,
            @PathVariable String sStamp,
            @RequestBody Sample sampleDetails) {

        LocalDateTime dateTime = LocalDateTime.parse(sStamp);

        return repository.findById(new SampleId(sId, dateTime))
                .map(sample -> {
                    sample.setName(sampleDetails.getName());
                    sample.setWeightNet(sampleDetails.getWeightNet());
                    sample.setWeightBru(sampleDetails.getWeightBru());
                    sample.setWeightTar(sampleDetails.getWeightTar());
                    sample.setQuantity(sampleDetails.getQuantity());
                    sample.setDistance(sampleDetails.getDistance());
                    sample.setDateCrumbled(sampleDetails.getDateCrumbled());
                    sample.setsFlags(sampleDetails.getsFlags());
                    sample.setLane(sampleDetails.getLane());
                    sample.setComment(sampleDetails.getComment());
                    sample.setDateExported(sampleDetails.getDateExported());
                    return ResponseEntity.ok(repository.save(sample));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{sId}/{sStamp}")
    public ResponseEntity<Void> delete(
            @PathVariable String sId,
            @PathVariable String sStamp) {

        LocalDateTime dateTime = LocalDateTime.parse(sStamp);

        return repository.findById(new SampleId(sId, dateTime))
                .map(sample -> {
                    repository.delete(sample);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
