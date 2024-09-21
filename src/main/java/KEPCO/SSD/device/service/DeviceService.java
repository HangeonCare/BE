package KEPCO.SSD.device.service;

import KEPCO.SSD.device.dto.DeviceRequestDto;
import KEPCO.SSD.device.dto.DeviceResponseDto;
import KEPCO.SSD.device.entity.Device;
import KEPCO.SSD.device.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public DeviceResponseDto registerDevice(Long userId, DeviceRequestDto requestDto) {
        Device device = new Device(userId, requestDto.getSerialNumber());
        deviceRepository.save(device);
        return new DeviceResponseDto("기기 등록 완료", device.getSerialNumber());
    }

    public DeviceResponseDto deleteDevice(Long userId, String serialNumber) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        deviceRepository.delete(device);
        return new DeviceResponseDto("기기 삭제 완료", serialNumber);
    }

    public List<DeviceResponseDto> getDevices(Long userId) {
        return deviceRepository.findByUserId(userId)
                .stream()
                .map(device -> new DeviceResponseDto("기기 조회 완료", device.getSerialNumber()))
                .collect(Collectors.toList());
    }

    public DeviceResponseDto setPeriod(Long userId, String serialNumber, String period) {
        Device device = deviceRepository.findByUserIdAndSerialNumber(userId, serialNumber)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 기기입니다."));
        device.setPeriod(period);
        deviceRepository.save(device);
        return new DeviceResponseDto("기간 설정 완료", serialNumber);
    }
}