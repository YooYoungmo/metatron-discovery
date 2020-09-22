package app.metatron.discovery.domain.idcube.imsi.repository;

import app.metatron.discovery.domain.idcube.imsi.entity.DataDownloadHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataDownloadHistoryRepository extends JpaRepository<DataDownloadHistory, Long> {
}