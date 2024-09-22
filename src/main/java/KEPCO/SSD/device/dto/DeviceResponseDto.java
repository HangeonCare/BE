package KEPCO.SSD.device.dto;

import lombok.Data;

@Data
public class DeviceResponseDto {
    private String message;
    private String serialNumber;

    public DeviceResponseDto(String message, String serialNumber, String period) {
        this.message = message;
        this.serialNumber = serialNumber;
    }
}