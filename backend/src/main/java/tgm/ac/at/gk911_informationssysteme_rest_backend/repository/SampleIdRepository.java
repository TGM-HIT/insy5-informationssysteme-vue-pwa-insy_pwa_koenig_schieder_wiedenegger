package tgm.ac.at.gk911_informationssysteme_rest_backend.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Sample;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.SampleId;

@Repository
public interface SampleIdRepository extends PagingAndSortingRepository<Sample, SampleId> {
    // Intentionally left without custom methods as requested.
}
