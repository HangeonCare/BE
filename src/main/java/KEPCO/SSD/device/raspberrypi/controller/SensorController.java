package KEPCO.SSD.device.raspberrypi.controller;

import KEPCO.SSD.device.raspberrypi.dto.SensorRequestDto;
import KEPCO.SSD.device.raspberrypi.dto.SensorResponseDto;
import KEPCO.SSD.device.raspberrypi.service.SensorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}")
public class SensorController {
    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PostMapping("/sensors")
    public ResponseEntity<SensorResponseDto> handleSensorData(
            @PathVariable int userId,
            @RequestBody SensorRequestDto sensorRequestDto) {

        // 센서 데이터 처리
        sensorService.processSensorData(userId, sensorRequestDto);

        // 응답 생성
        SensorResponseDto responseDto = new SensorResponseDto("감지 완료", sensorRequestDto.getSerialNumber());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}
