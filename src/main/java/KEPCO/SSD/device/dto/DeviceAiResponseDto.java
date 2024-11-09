package KEPCO.SSD.device.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeviceAiResponseDto {
    private int month;
    private List<List<Integer>> monthlyEventCounts;

    public DeviceAiResponseDto(int month, List<List<Integer>> monthlyEventCounts) {
        this.month = month;
        this.monthlyEventCounts = monthlyEventCounts;
    }
}
