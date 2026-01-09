package tgm.ac.at.gk911_informationssysteme_rest_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tgm.ac.at.gk911_informationssysteme_rest_backend.entity.Log;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
}
