package KEPCO.SSD.device.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PeriodRequestDto {
        private int day;
        private int hour;
}