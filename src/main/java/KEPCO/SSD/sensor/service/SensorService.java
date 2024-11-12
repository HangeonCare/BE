package KEPCO.SSD.sensor.service;

import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.entity.SensorData;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.device.repository.SensorDataRepository;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository;
import KEPCO.SSD.user.service.SmsService;
import KEPCO.SSD.sensor.dto.SensorRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final Map<String, Map<LocalDateTime, Integer>> eventTimesMap = new ConcurrentHashMap<>();
    private static final int[] EVENT_HOURS = {6, 12, 18, 24};

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
            lastDetectedTimeMap.put(sensorRequestDto.getSerialNumber(), System.currentTimeMillis());
            lastAlertTimeMap.put(serialNumber, 0L);

            device.setAction(true);
            deviceRepository.save(device);
        } else if (sensorRequestDto.getValue() == 1) {
            if (device.isAction()) {
                // 문이 닫히는 경우에만 카운트를 기록
                eventTimesMap.putIfAbsent(serialNumber, new ConcurrentHashMap<>());
                int hourBucket = getClosestHourBucket(now.getHour());
                LocalDateTime timeKey = now.withHour(hourBucket).withMinute(0).withSecond(0).withNano(0);
                eventTimesMap.get(serialNumber).merge(timeKey, 1, Integer::sum);

                // 이벤트 데이터를 저장
                Map<LocalDateTime, Integer> events = eventTimesMap.get(serialNumber);
                try {
                    String eventCountsJson = objectMapper.writeValueAsString(events);

                    SensorData sensorData = new SensorData();
                    sensorData.setUserId((long) userId);
                    sensorData.setSerialNumber(serialNumber);
                    sensorData.setEventCounts(eventCountsJson);
                    sensorData.setTime(LocalDateTime.now());

                    sensorDataRepository.save(sensorData);
                } catch (Exception e) {
                    logger.error("이벤트 데이터를 저장하는 중 오류 발생", e);
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
        for (int hour : EVENT_HOURS) {
            if (currentHour < hour) {
                return hour - 1;
            }
        }
        return EVENT_HOURS[EVENT_HOURS.length - 1] - 1;
    }

    public Map<String, Map<LocalDateTime, Integer>> getEventTimesMap() {
        return eventTimesMap;
    }
}