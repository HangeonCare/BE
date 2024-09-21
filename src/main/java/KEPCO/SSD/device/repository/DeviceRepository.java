package KEPCO.SSD.device.repository;

import KEPCO.SSD.device.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByUserId(int userId); // int로 변경
    Optional<Device> findByUserIdAndSerialNumber(int userId, String serialNumber); // int로 변경
    void deleteByUserIdAndSerialNumber(int userId, String serialNumber); // int로 변경
}
