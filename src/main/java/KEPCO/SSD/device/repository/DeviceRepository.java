package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByUserId(Long userId);
    Optional<Device> findByUserIdAndSerialNumber(Long userId, String serialNumber);
    void deleteByUserIdAndSerialNumber(Long userId, String serialNumber);
}