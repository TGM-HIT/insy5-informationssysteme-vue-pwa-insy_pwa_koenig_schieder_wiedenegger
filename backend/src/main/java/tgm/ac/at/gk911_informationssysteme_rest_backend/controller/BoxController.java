package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Box;
import tgm.ac.at.gk911_informationssysteme_rest_backend.repository.BoxRepository;

import java.util.List;

@RestController
@RequestMapping("/api/box")
@CrossOrigin(origins = "*")
public class BoxController {

    @Autowired
    private BoxRepository repository;

    @GetMapping
    public List<Box> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Box> getById(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Box create(@RequestBody Box box) {
        return repository.save(box);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Box> update(
            @PathVariable String id,
            @RequestBody Box boxDetails) {

        return repository.findById(id)
                .map(box -> {
                    box.setName(boxDetails.getName());
                    box.setNumMax(boxDetails.getNumMax());
                    box.setType(boxDetails.getType());
                    box.setComment(boxDetails.getComment());
                    box.setDateExported(boxDetails.getDateExported());
                    return ResponseEntity.ok(repository.save(box));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        return repository.findById(id)
                .map(box -> {
                    repository.delete(box);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
