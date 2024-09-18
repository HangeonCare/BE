package KEPCO.SSD.device.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;
import jakarta.persistence.Id;

@Entity
@Data
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String serialNumber;
    private String period;

    public Device(Long userId, String serialNumber) {
        this.userId = userId;
        this.serialNumber = serialNumber;
    }

}