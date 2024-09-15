package KEPCO.SSD.user.service;

import KEPCO.SSD.user.dto.Request.LoginRequestDto;
import KEPCO.SSD.user.dto.Response.SignupResponseDto;
import KEPCO.SSD.user.dto.Request.UserDto;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public SignupResponseDto signup(UserDto userDto) {
        String phoneNumber = userDto.getPhoneNumber();
        String password = userDto.getPassword();

        String encodedPassword = passwordEncoder.encode(password);

        boolean isPhoneNumberDuplicated = userRepository.existsByPhoneNumber(phoneNumber);
        if (isPhoneNumberDuplicated) {
            throw new IllegalArgumentException("이미 존재하는 계정입니다.");
        }

        User user = new User();
        user.setPhoneNumber(phoneNumber);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return new SignupResponseDto("회원가입 완료", phoneNumber);
    }

    // 로그인
    public User login(LoginRequestDto loginRequestDto) {
        String phoneNumber = loginRequestDto.getPhoneNumber();
        String password = loginRequestDto.getPassword();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }
}