package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.user.service.SmsService;
import KEPCO.SSD.sensor.dto.SensorRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class SensorService {

    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);
    private final DeviceRepository deviceRepository;
    private final SmsService smsService;
    private long lastDetectedTime;

    public SensorService(DeviceRepository deviceRepository, SmsService smsService) {
        this.deviceRepository = deviceRepository;
        this.smsService = smsService;
        this.lastDetectedTime = System.currentTimeMillis();
    }

    public void processSensorData(int userId, SensorRequestDto sensorRequestDto) {
        logger.info("Received sensor data from user {}", userId);
        logger.info("Serial Number: {}", sensorRequestDto.getSerialNumber());
        logger.info("Value: {}", sensorRequestDto.getValue());

        if (sensorRequestDto.getValue() == 0) {
            lastDetectedTime = System.currentTimeMillis();
        }
        else if (sensorRequestDto.getValue() == 1) {
            Device device = deviceRepository.findByUserIdAndSerialNumber(userId, sensorRequestDto.getSerialNumber())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));

            String period = device.getPeriod();

            if (isExceededPeriod(period)) {
                smsService.sendSms(String.valueOf(userId), "설정된 기간 동안 움직임이 감지되지 않았습니다.");
            }
        }
    }

    private boolean isExceededPeriod(String period) {
        long detectionPeriod = Long.parseLong(period);
        long currentTime = System.currentTimeMillis();

        return (currentTime - lastDetectedTime) > detectionPeriod * 60_000;
    }
}