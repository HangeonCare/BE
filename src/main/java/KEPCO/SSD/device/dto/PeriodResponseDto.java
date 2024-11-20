package KEPCO.SSD.device.dto;
import lombok.Data;

@Data
public class PeriodResponseDto {
    private String message;
    private int day;
    private int hour;

    public PeriodResponseDto(String message, int day, int hour) {
        this.message = message;
        this.day = day;
        this.hour = hour;
    }
}