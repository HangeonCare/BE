package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.entity.SensorData;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.device.repository.SensorDataRepository;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository;
import KEPCO.SSD.user.service.SmsService;
import KEPCO.SSD.sensor.dto.SensorRequestDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SensorService {

    private static final long ALERT_COOLDOWN_PERIOD = 300_000;
    private static final Logger logger = LoggerFactory.getLogger(SensorService.class);
    private final SmsService smsService;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final SensorDataRepository sensorDataRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, Long> lastDetectedTimeMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAlertTimeMap = new ConcurrentHashMap<>();
    private static final int[] EVENT_HOURS = {6, 12, 18, 24};

    private final Map<String, Boolean> isDoorClosedMap = new ConcurrentHashMap<>();

    public SensorService(SmsService smsService, DeviceRepository deviceRepository, UserRepository userRepository, SensorDataRepository sensorDataRepository) {
        this.smsService = smsService;
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.sensorDataRepository = sensorDataRepository;
    }

    public void processSensorData(int userId, SensorRequestDto sensorRequestDto) {
        logger.info("Received sensor data from user {}", userId);
        logger.info("Serial Number: {}", sensorRequestDto.getSerialNumber());
        logger.info("Value: {}", sensorRequestDto.getValue());

        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, sensorRequestDto.getSerialNumber())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));

        LocalDateTime now = LocalDateTime.now();
        String serialNumber = device.getSerialNumber();

        if (sensorRequestDto.getValue() == 0) {
            isDoorClosedMap.put(serialNumber, true);

            lastDetectedTimeMap.put(sensorRequestDto.getSerialNumber(), System.currentTimeMillis());
            lastAlertTimeMap.put(serialNumber, 0L);

            device.setAction(true);
            deviceRepository.save(device);
        } else if (sensorRequestDto.getValue() == 1) {
            if (isDoorClosedMap.getOrDefault(serialNumber, false)) {
                isDoorClosedMap.put(serialNumber, false);
                int hourBucket = getClosestHourBucket(now.getHour());
                try {
                    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
                    SensorData sensorData = sensorDataRepository.findByUserIdAndSerialNumberAndTime((long) userId, serialNumber, todayStart)
                            .orElseGet(() -> {
                                try {
                                    SensorData newSensorData = new SensorData();
                                    newSensorData.setUserId((long) userId);
                                    newSensorData.setSerialNumber(serialNumber);
                                    newSensorData.setEventCounts(objectMapper.writeValueAsString(initializeHourCounts())); // Initialize counts
                                    newSensorData.setTime(todayStart);
                                    return newSensorData;
                                } catch (IOException e) {
                                    logger.error("IOException occurred while creating new SensorData", e);
                                    return null;
                                }
                            });
                    if (sensorData != null) {
                        Map<String, Integer> hourCounts = objectMapper.readValue(sensorData.getEventCounts(), new TypeReference<Map<String, Integer>>() {});
                        hourCounts.put(String.valueOf(hourBucket), hourCounts.getOrDefault(String.valueOf(hourBucket), 0) + 1);
                        sensorData.setEventCounts(objectMapper.writeValueAsString(hourCounts));
                        sensorDataRepository.save(sensorData);
                    }

                } catch (IOException e) {
                    logger.error("IOException occurred while processing sensor data", e);
                }
            }
            int period = device.getPeriod();

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

    private int getClosestHourBucket(int currentHour) {
        if (currentHour < 6) return 0;
        if (currentHour < 12) return 1;
        if (currentHour < 18) return 2;
        return 3;
    }

    private Map<String, Integer> initializeHourCounts() {
        Map<String, Integer> hourCounts = new HashMap<>();
        for (int i = 0; i < EVENT_HOURS.length; i++) {
            hourCounts.put(String.valueOf(i), 0);
        }
        return hourCounts;
    }
}