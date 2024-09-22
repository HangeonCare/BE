package KEPCO.SSD.device.dto;
import lombok.Data;

@Data
public class PeriodResponseDto {
    private String message;
    private String period;
    private String serialNumber;

    public PeriodResponseDto(String message, String serialNumber, String period) {
        this.message = message;
        this.period = period;
        this.serialNumber = serialNumber;
    }
}