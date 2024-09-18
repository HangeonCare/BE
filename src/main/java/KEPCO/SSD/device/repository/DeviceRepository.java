package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByUserId(Long userId);
    Device findByUserIdAndSerialNumber(Long userId, String serialNumber);
    void deleteByUserIdAndSerialNumber(Long userId, String serialNumber);
}