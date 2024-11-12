package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Integer> {
    List<SensorData> findByUserIdAndSerialNumberOrderByTimeDesc(Long userId, String serialNumber);

    Optional<SensorData> findByUserIdAndSerialNumberAndDate(int userId, String serialNumber, LocalDateTime todayStart);
}