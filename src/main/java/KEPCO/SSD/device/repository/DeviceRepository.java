package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByUserId(long userId);
    Optional<Device> findByUserIdAndSerialNumber(long userId, String serialNumber);
    void deleteByUserIdAndSerialNumber(long userId, String serialNumber);
}