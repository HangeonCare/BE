package KEPCO.SSD.device.dto;

import lombok.Data;

@Data
public class DeviceGetResponseDto {
    private String message;
    private String serialNumber;
    private boolean action;

    public DeviceGetResponseDto(String message, String serialNumber,boolean action) {
        this.message = message;
        this.serialNumber = serialNumber;
        this.action = action;
    }
}
