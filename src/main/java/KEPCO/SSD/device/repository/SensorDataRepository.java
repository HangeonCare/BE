package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Integer> {
    List<SensorData> findByUserIdAndSerialNumberOrderByTimeDesc(Long userId, String serialNumber);
}