package KEPCO.SSD.user.controller;

import KEPCO.SSD.user.dto.Request.SignupRequestDto;
import KEPCO.SSD.user.dto.Request.LoginRequestDto;
import KEPCO.SSD.user.dto.Request.SendCodeRequestDto;
import KEPCO.SSD.user.dto.Response.SignupResponseDto;
import KEPCO.SSD.user.dto.Response.LoginResponseDto;
import KEPCO.SSD.user.entity.User;
import KEPCO.SSD.user.repository.UserRepository;
import KEPCO.SSD.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/send-code")
    public ResponseEntity<String> sendCode(@RequestBody @Valid SendCodeRequestDto request) {
        userService.sendVerificationCode(request.getPhoneNumber());
        return ResponseEntity.ok("인증메세지 발송");
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody @Valid SignupRequestDto request) {
        try {
            userService.verifyCode(request.getPhoneNumber(), request.getVerificationCode());
            userService.signup(request);
            return new ResponseEntity<>(new SignupResponseDto("회원가입 완료", request.getPhoneNumber()), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new SignupResponseDto(e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request) {
        try {
            User user = userService.login(request);
            return new ResponseEntity<>(new LoginResponseDto((int) user.getUserId(), "로그인 성공"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new LoginResponseDto(-1, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return new ResponseEntity<>("로그아웃 완료", HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<User> getAllUser(){
        return userRepository.findAll();
    }
}