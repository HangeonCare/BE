package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository; // UserRepository 추가
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
    private final UserRepository userRepository; // UserRepository 추가

    private final Map<String, Long> lastDetectedTimeMap = new HashMap<>();

    public SensorService(DeviceRepository deviceRepository, SmsService smsService, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.smsService = smsService;
        this.userRepository = userRepository; // UserRepository 주입
    }

    public void processSensorData(int userId, SensorRequestDto sensorRequestDto) {
        logger.info("Received sensor data from user {}", userId);
        logger.info("Serial Number: {}", sensorRequestDto.getSerialNumber());
        logger.info("Value: {}", sensorRequestDto.getValue());

        if (sensorRequestDto.getValue() == 0) {
            Device device = deviceRepository.findByUserIdAndSerialNumber(userId, sensorRequestDto.getSerialNumber())
                    .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));

            lastDetectedTimeMap.put(sensorRequestDto.getSerialNumber(), System.currentTimeMillis());
        }
    }

    @Scheduled(initialDelay = 0, fixedDelay = 1000)
    private void checkPeriodExceeded() {
        Iterable<Device> allDevices = deviceRepository.findAll();

        for (Device device : allDevices) {
            int userId = Math.toIntExact(device.getUserId());
            int period = device.getPeriod();
            String serialNumber = device.getSerialNumber();

            if (isExceededPeriod(serialNumber, period)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    String phoneNumber = user.getPhoneNumber();
                    smsService.sendSms(phoneNumber, "SSD[고독사 방지 시스템] 설정된 기간 동안 움직임이 감지되지 않았습니다.");
                } else {
                    logger.warn("사용자를 찾을 수 없습니다. userId: {}", userId);
                }
            }
        }
    }

    private boolean isExceededPeriod(String serialNumber, int period) {
        Long lastDetectedTime = lastDetectedTimeMap.get(serialNumber);
        long currentTime = System.currentTimeMillis();
        return (lastDetectedTime == null || (currentTime - lastDetectedTime) > period * 60_000);
    }
}