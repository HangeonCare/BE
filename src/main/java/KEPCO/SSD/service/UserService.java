package KEPCO.SSD.service;

import KEPCO.SSD.dto.LoginRequestDto;
import KEPCO.SSD.dto.SignupResponseDto;
import KEPCO.SSD.dto.UserDto;
import KEPCO.SSD.entity.User;
import KEPCO.SSD.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import javax.servlet.http.HttpServletRequest;

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

    // 회원 탈퇴
    public void signOut(LoginRequestDto signOutLoginRequestDto) {
        String phoneNumber = signOutLoginRequestDto.getPhoneNumber();
        String password = signOutLoginRequestDto.getPassword();

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("전화번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.deleteById(user.getUserId());
    }

    // 로그아웃
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
    }
}
