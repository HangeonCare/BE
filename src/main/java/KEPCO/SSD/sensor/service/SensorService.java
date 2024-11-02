package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository;
import KEPCO.SSD.user.service.SmsService;
import KEPCO.SSD.sensor.dto.SensorRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SensorService {

    private static final long ALERT_COOLDOWN_PERIOD = 300_000;
    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);
    private final DeviceRepository deviceRepository;
    private final SmsService smsService;
    private final UserRepository userRepository;

    private final Map<String, Long> lastDetectedTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAlertTimeMap = new ConcurrentHashMap<>();

    public SensorService(DeviceRepository deviceRepository, SmsService smsService, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.smsService = smsService;
        this.userRepository = userRepository; // UserRepository 주입
    }

    public void processSensorData(int userId, SensorRequestDto sensorRequestDto) {
        logger.info("Received sensor data from user {}", userId);
        logger.info("Serial Number: {}", sensorRequestDto.getSerialNumber());
        logger.info("Value: {}", sensorRequestDto.getValue());

        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, sensorRequestDto.getSerialNumber())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));

        if (sensorRequestDto.getValue() == 0) {
            String serialNumber = device.getSerialNumber();
            lastDetectedTimeMap.put(sensorRequestDto.getSerialNumber(), System.currentTimeMillis());
            lastAlertTimeMap.put(serialNumber, 0L);

            device.setAction(true);
            deviceRepository.save(device);
        } else if (sensorRequestDto.getValue() == 1) {
            int period = device.getPeriod();
            String serialNumber = device.getSerialNumber();

            if (isExceededPeriod(serialNumber, period)&& canSendAlert(serialNumber)) {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    String phoneNumber = user.getPhoneNumber();
                    smsService.sendSms(phoneNumber, String.format("SSD [고독사 방지 시스템]\n%s(이)가 설정된 기간 동안 움직임을 감지하지 못했습니다.", serialNumber));
                    lastAlertTimeMap.put(serialNumber, System.currentTimeMillis());

                    device.setAction(false);
                    deviceRepository.save(device);
                } else {
                    logger.warn("사용자를 찾을 수 없습니다. userId: {}", userId);
                }
            }
        }
    }
    private boolean isExceededPeriod(String serialNumber, int period) {
        Long lastDetectedTime = lastDetectedTimeMap.get(serialNumber);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastDetectedTime) > period * 60_000;
    }
    private boolean canSendAlert(String serialNumber) {
        Long lastAlertTime = lastAlertTimeMap.get(serialNumber);
        long currentTime = System.currentTimeMillis();
        return lastAlertTime == 0 || (currentTime - lastAlertTime) > ALERT_COOLDOWN_PERIOD;
    }
}