package KEPCO.SSD.device.controller;

import KEPCO.SSD.device.dto.DeviceGetResponseDto;
import KEPCO.SSD.device.dto.DeviceRegisterRequestDto;
import KEPCO.SSD.device.dto.DeviceResponseDto;
import KEPCO.SSD.device.dto.DeviceAiResponseDto;
import KEPCO.SSD.device.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.*;

@RestController
@RequestMapping("/users/{userId}/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    // 기기 등록
    @PostMapping
    public DeviceResponseDto registerDevice(@PathVariable Long userId, @RequestBody DeviceRegisterRequestDto requestDto) {
        return deviceService.registerDevice(userId, requestDto);
    }

    // 기기 삭제
    @DeleteMapping("/{serialNumber}")
    public DeviceResponseDto deleteDevice(@PathVariable Long userId, @PathVariable String serialNumber) {
        return deviceService.deleteDevice(userId, serialNumber);
    }

    // 기기 조회
    @GetMapping
    public List<DeviceGetResponseDto> getDevices(@PathVariable Long userId) {
        return deviceService.getDevices(userId);
    }

    // ai
    @GetMapping("/{serialNumber}/ai")
    public DeviceAiResponseDto getOpenCloseTimes(
            @PathVariable Long userId,
            @PathVariable String serialNumber) {
        return deviceService.getOpenCloseTimes(userId, serialNumber);
    }
}