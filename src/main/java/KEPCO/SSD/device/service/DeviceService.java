package KEPCO.SSD.device.service;

import KEPCO.SSD.device.dto.DeviceAiResponseDto;
import KEPCO.SSD.device.dto.DeviceGetResponseDto;
import KEPCO.SSD.device.dto.DeviceRegisterRequestDto;
import KEPCO.SSD.device.dto.DeviceResponseDto;
import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.entity.SensorData;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.device.repository.SensorDataRepository;
import KEPCO.SSD.sensor.service.SensorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SensorService sensorService;

    public DeviceService(DeviceRepository deviceRepository, SensorDataRepository sensorDataRepository, SensorService sensorService) {
        this.deviceRepository = deviceRepository;
        this.sensorDataRepository = sensorDataRepository;
        this.sensorService = sensorService;
    }

    public DeviceResponseDto registerDevice(Long userId, DeviceRegisterRequestDto requestDto) {
        if (requestDto.getSerialNumber() == null || requestDto.getSerialNumber().isEmpty()) {
            throw new IllegalArgumentException("유효한 시리얼 번호를 입력하세요.");
        }

        Device device = new Device(userId, requestDto.getSerialNumber());
        device.setAction(true);
        device.setPeriod(1);
        device.setDay(1);
        device.setHour(0);
        deviceRepository.save(device);
        return new DeviceResponseDto("기기 등록 완료", device.getSerialNumber(), device.getDay(), device.getHour());
    }

    public DeviceResponseDto deleteDevice(Long userId, String serialNumber) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        deviceRepository.delete(device);
        return new DeviceResponseDto("기기 삭제 완료", serialNumber, device.getDay(), device.getHour());
    }

    public List<DeviceGetResponseDto> getDevices(Long userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(device -> new DeviceGetResponseDto("기기 조회 완료", device.getSerialNumber(), device.isAction()))
                .collect(Collectors.toList());
    }

    public DeviceResponseDto setPeriod(Long userId, String serialNumber, int day, int hour) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        device.setDay(day);
        device.setHour(hour);
        deviceRepository.save(device);
        return new DeviceResponseDto("기간 설정 완료", serialNumber, device.getDay(), device.getHour());
    }

    public DeviceAiResponseDto getOpenCloseTimes(Long userId, String serialNumber) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        List<SensorData> sensorDataList = sensorDataRepository.findByUserIdAndSerialNumberOrderByTimeDesc(userId, serialNumber);

        List<SensorData> filteredData = sensorDataList.stream()
                .filter(data -> {
                    LocalDateTime dataTime = data.getTime();
                    return dataTime.isBefore(now.toLocalDate().atStartOfDay()) && dataTime.isAfter(sevenDaysAgo);
                })
                .collect(Collectors.toList());

        List<List<Integer>> eventCounts = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            List<Integer> dayCounts = new ArrayList<>(Collections.nCopies(4, 0));
            if (i < filteredData.size()) {
                String eventCountsJson = filteredData.get(i).getEventCounts();
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Integer> eventCountsMap = objectMapper.readValue(eventCountsJson, Map.class);

                    for (int j = 0; j < 4; j++) {
                        String key = String.valueOf(j);
                        dayCounts.set(j, eventCountsMap.getOrDefault(key, 0));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            eventCounts.add(dayCounts);
        }

        return new DeviceAiResponseDto(eventCounts);
    }
}