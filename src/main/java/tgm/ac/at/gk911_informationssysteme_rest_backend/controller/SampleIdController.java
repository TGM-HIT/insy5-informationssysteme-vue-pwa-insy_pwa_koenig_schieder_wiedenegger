package tgm.ac.at.gk911_informationssysteme_rest_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(value = "/api/sample-ids", produces = "application/json")
@Tag(name = "SampleID", description = "Read endpoints for distinct Sample IDs from venlab.sample")
public class SampleIdController {

    @PersistenceContext
    private EntityManager em;

    public SampleIdController() {}

    @GetMapping
    @Operation(summary = "List distinct Sample IDs")
    public Page<String> list(@PageableDefault(size = 20) Pageable pageable) {
        // Data query
        TypedQuery<String> dataQuery = em.createQuery(
                "select distinct s.id.sId from Sample s order by s.id.sId", String.class);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<String> content = dataQuery.getResultList();

        // Count query
        Long total = em.createQuery(
                "select count(distinct s.id.sId) from Sample s", Long.class)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    @GetMapping("/{sId}/stamps")
    @Operation(summary = "List stamps (s_stamp) for a given Sample ID, newest first")
    public Page<LocalDateTime> listStamps(
            @PathVariable String sId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        // Existence check
        Long cnt = em.createQuery(
                        "select count(s) from Sample s where s.id.sId = :sId", Long.class)
                .setParameter("sId", sId)
                .getSingleResult();
        if (cnt == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sample ID not found");
        }

        // Data query
        TypedQuery<LocalDateTime> dataQuery = em.createQuery(
                "select s.id.sStamp from Sample s where s.id.sId = :sId order by s.id.sStamp desc",
                LocalDateTime.class);
        dataQuery.setParameter("sId", sId);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<LocalDateTime> content = dataQuery.getResultList();

        // Count query
        Long total = em.createQuery(
                        "select count(s) from Sample s where s.id.sId = :sId", Long.class)
                .setParameter("sId", sId)
                .getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
