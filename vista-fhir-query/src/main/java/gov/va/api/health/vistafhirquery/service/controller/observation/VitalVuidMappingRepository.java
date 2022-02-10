package gov.va.api.health.vistafhirquery.service.controller.observation;

import gov.va.api.health.autoconfig.logging.Loggable;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

/** Repository for interacting with the VitalVuidMapping table. */
@Loggable
public interface VitalVuidMappingRepository
    extends JpaSpecificationExecutor<VitalVuidMappingEntity>,
        PagingAndSortingRepository<VitalVuidMappingEntity, String> {
  List<VitalVuidMappingEntity> findByCodingSystemId(Short codingSystemId);
}
