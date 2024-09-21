package KEPCO.SSD.sensor.controller;

import KEPCO.SSD.sensor.dto.SensorRequestDto;
import KEPCO.SSD.sensor.dto.SensorResponseDto;
import KEPCO.SSD.sensor.service.SensorService;
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
            @PathVariable long userId,
            @RequestBody SensorRequestDto sensorRequestDto) {

        sensorService.processSensorData((int) userId, sensorRequestDto);

        SensorResponseDto responseDto = new SensorResponseDto("감지 완료", sensorRequestDto.getSerialNumber());
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}