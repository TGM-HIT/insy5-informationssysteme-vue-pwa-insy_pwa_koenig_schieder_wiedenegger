package tgm.ac.at.gk911_informationssysteme_rest_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.BoxPos;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.BoxPosId;

@Repository
public interface BoxPosRepository extends JpaRepository<BoxPos, BoxPosId> {
    Page<BoxPos> findById_BId(String bId, Pageable pageable);
}
