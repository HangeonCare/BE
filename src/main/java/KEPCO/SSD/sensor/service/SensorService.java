package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.user.service.SmsService;
import KEPCO.SSD.sensor.dto.SensorRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class SensorService {

    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);
    private final DeviceRepository deviceRepository;
    private final SmsService smsService;

    // 각 기기별 마지막 감지 시간을 저장하는 Map
    private final Map<String, Long> lastDetectedTimeMap = new HashMap<>();

    public SensorService(DeviceRepository deviceRepository, SmsService smsService) {
        this.deviceRepository = deviceRepository;
        this.smsService = smsService;
    }

    // 센서 데이터 처리
    public void processSensorData(int userId, SensorRequestDto sensorRequestDto) {
        logger.info("Received sensor data from user {}", userId);
        logger.info("Serial Number: {}", sensorRequestDto.getSerialNumber());
        logger.info("Value: {}", sensorRequestDto.getValue());

        if (sensorRequestDto.getValue() == 0) {
            lastDetectedTimeMap.put(sensorRequestDto.getSerialNumber(), System.currentTimeMillis());
        } else {
            Device device = deviceRepository.findByUserIdAndSerialNumber(userId, sensorRequestDto.getSerialNumber())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
            int period = device.getPeriod();

            if (isExceededPeriod(sensorRequestDto.getSerialNumber(), period)) {
                smsService.sendSms(String.valueOf(userId), "설정된 기간 동안 움직임이 감지되지 않았습니다.");
            }
        }
    }

    // 주기적으로 확인
    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    private void checkPeriodExceeded() {
        Iterable<Device> allDevices = deviceRepository.findAll();

        for (Device device : allDevices) {
            int userId = Math.toIntExact(device.getUserId());
            int period = device.getPeriod();
            String serialNumber = device.getSerialNumber();

            if (isExceededPeriod(serialNumber, period)) {
                smsService.sendSms(String.valueOf(userId), "설정된 기간 동안 움직임이 감지되지 않았습니다.");
            }
        }
    }

    // 기간 확인
    private boolean isExceededPeriod(String serialNumber, int period) {
        Long lastDetectedTime = lastDetectedTimeMap.get(serialNumber);

        if (lastDetectedTime == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastDetectedTime) > period * 60_000;
    }
}
