package KEPCO.SSD.device.raspberrypi.dto;

import lombok.Data;

@Data
public class SensorRequestDto {
    private String serialNumber;
    private int value;
}