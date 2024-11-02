package KEPCO.SSD.device.service;

import KEPCO.SSD.device.dto.DeviceRegisterRequestDto;
import KEPCO.SSD.device.dto.DeviceResponseDto;
import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
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
        return new DeviceResponseDto("기기 등록 완료", device.getSerialNumber(), device.getDay(), device.getHour(), device.isAction());
    }

    public DeviceResponseDto deleteDevice(Long userId, String serialNumber) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        deviceRepository.delete(device);
        return new DeviceResponseDto("기기 삭제 완료", serialNumber, device.getDay(), device.getHour(), device.isAction());
    }

    public List<DeviceResponseDto> getDevices(Long userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(device -> new DeviceResponseDto("기기 조회 완료", device.getSerialNumber(), device.getDay(), device.getHour(), device.isAction()))
                .collect(Collectors.toList());
    }

    public DeviceResponseDto setPeriod(Long userId, String serialNumber, int day, int hour) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        device.setDay(day);
        device.setHour(hour);
        deviceRepository.save(device);
        return new DeviceResponseDto("기간 설정 완료", serialNumber, device.getDay(), device.getHour(), device.isAction());
    }
}