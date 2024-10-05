package KEPCO.SSD.device.dto;
import lombok.Data;

@Data
public class PeriodResponseDto {
    private String message;
    private int day;
    private int hour;
    private String serialNumber;

    public PeriodResponseDto(String message, String serialNumber, int day, int hour) {
        this.message = message;
        this.day = day;
        this.hour = hour;
        this.serialNumber = serialNumber;
    }
}