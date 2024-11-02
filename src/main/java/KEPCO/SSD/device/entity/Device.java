package KEPCO.SSD.device.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "devices")
public class Device {

    @Id
    @Column(name = "serial_number")
    private String serialNumber;
    private Long userId;
    private boolean action;
    private int day;
    private int hour;
    private int period;

    public Device(Long userId, String serialNumber) {
        this.userId = userId;
        this.serialNumber = serialNumber;
    }
}