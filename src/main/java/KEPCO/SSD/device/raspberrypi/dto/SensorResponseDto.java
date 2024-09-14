package KEPCO.SSD.device.raspberrypi.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SensorResponseDto {
    private String message;
    private String serialNumber;
}