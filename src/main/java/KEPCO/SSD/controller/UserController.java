package KEPCO.SSD.controller;

import KEPCO.SSD.dto.LoginRequestDto;
import KEPCO.SSD.dto.SignupResponseDto;
import KEPCO.SSD.dto.LoginResponseDto;
import KEPCO.SSD.dto.UserDto;
import KEPCO.SSD.entity.User;
import KEPCO.SSD.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*") // CORS 설정을 위해 추가
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(@RequestBody @Valid UserDto request) {
        try {
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
            return new ResponseEntity<>(new LoginResponseDto(user.getUserId(), "로그인 성공"), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new LoginResponseDto(-1, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<SignupResponseDto> signOut(@RequestBody @Valid LoginRequestDto request) {
        try {
            userService.signOut(request);
            return new ResponseEntity<>(new SignupResponseDto("회원탈퇴 완료", null), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new SignupResponseDto(e.getMessage(), null), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return new ResponseEntity<>("로그아웃 성공. 클라이언트 측에서 토큰을 삭제하세요.", HttpStatus.OK);
    }
}
