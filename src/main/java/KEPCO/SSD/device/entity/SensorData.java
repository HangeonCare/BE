package KEPCO.SSD.device.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int dataId;

    private Long userId;
    private String serialNumber;

    @Column(columnDefinition = "JSON")
    private String eventCounts;
    private LocalDateTime time;
}