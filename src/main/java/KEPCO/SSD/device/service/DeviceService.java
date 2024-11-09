package KEPCO.SSD.device.service;

import KEPCO.SSD.device.dto.DeviceAiResponseDto;
import KEPCO.SSD.device.dto.DeviceGetResponseDto;
import KEPCO.SSD.device.dto.DeviceRegisterRequestDto;
import KEPCO.SSD.device.dto.DeviceResponseDto;
import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import KEPCO.SSD.sensor.service.SensorService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final SensorService sensorService;

    public DeviceService(DeviceRepository deviceRepository, SensorService sensorService) {
        this.sensorService = sensorService;
        this.deviceRepository = deviceRepository;
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

    public DeviceAiResponseDto getMonthlyOpenCloseTimes(Long userId, String serialNumber, int month) {
        Map<LocalDateTime, Integer> eventTimes = sensorService.getEventTimesMap().get(serialNumber);

        List<List<Integer>> monthlyEventCounts = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), month);
        int daysInMonth = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            List<Integer> eventCountsForDay = new ArrayList<>(Collections.nCopies(24, 0));

            for (int hour = 0; hour < 24; hour++) {
                LocalTime time = LocalTime.of(hour, 0);
                LocalDateTime timeKey = time.atDate(yearMonth.atDay(day));  // 해당 날짜의 시간에 대한 LocalDateTime

                eventCountsForDay.set(hour, eventTimes != null ? eventTimes.getOrDefault(timeKey, 0) : 0);
            }

            monthlyEventCounts.add(eventCountsForDay);
        }

        return new DeviceAiResponseDto(month, monthlyEventCounts);
    }
}