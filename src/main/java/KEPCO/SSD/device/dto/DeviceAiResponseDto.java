package KEPCO.SSD.device.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeviceAiResponseDto {
    private List<List<Integer>> eventCounts;

    public DeviceAiResponseDto(List<List<Integer>> eventCounts) {
        this.eventCounts = eventCounts;
    }
}
