package KEPCO.SSD.user.service;

import KEPCO.SSD.user.dto.Request.LoginRequestDto;
import KEPCO.SSD.user.dto.Request.SignupRequestDto;
import KEPCO.SSD.user.dto.Response.SignupResponseDto;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.entity.VerificationCode;
import KEPCO.SSD.user.repository.UserRepository;
import KEPCO.SSD.user.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    // 인증번호 발송 및 저장
    public void sendVerificationCode(String phoneNumber) {
        String verificationCode = generateVerificationCode();
        smsService.sendSms(phoneNumber, "SSD [고독사 방지 시스템]\n인증번호: " + verificationCode);

        VerificationCode codeEntry = new VerificationCode();
        codeEntry.setPhoneNumber(phoneNumber);
        codeEntry.setVerificationCode(verificationCode);
        codeEntry.setSentAt(LocalDateTime.now());

        verificationCodeRepository.save(codeEntry);
    }

    // 인증번호 확인 로직
    public void verifyCode(String phoneNumber, String verificationCode) {
        VerificationCode verificationCodeEntry = verificationCodeRepository.findByPhoneNumber(phoneNumber);

        if (verificationCodeEntry == null || !verificationCodeEntry.getVerificationCode().equals(verificationCode)) {
            throw new IllegalArgumentException("잘못된 인증번호입니다.");
        }

        if (LocalDateTime.now().isAfter(verificationCodeEntry.getSentAt().plusMinutes(10))) {
            throw new IllegalArgumentException("인증번호가 만료되었습니다.");
        }
    }

    // 회원가입
    public SignupResponseDto signup(SignupRequestDto signupRequestDto) {
        String phoneNumber = signupRequestDto.getPhoneNumber();
        String password = signupRequestDto.getPassword();
        String confirmPassword = signupRequestDto.getConfirmPassword();

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        String encodedPassword = passwordEncoder.encode(password);
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
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

    // 인증번호 생성
    private String generateVerificationCode() {
        return String.valueOf((int) (Math.random() * 1000000)); // 6자리 랜덤 숫자
    }
}