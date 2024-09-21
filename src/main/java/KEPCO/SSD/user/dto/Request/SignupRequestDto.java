package KEPCO.SSD.user.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignupRequestDto {
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;
    @NotBlank(message = "인증번호는 필수 입력 값입니다.")
    private String verificationCode;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
    @NotBlank(message = "비밀번호 재 입력은 필수 입력 값입니다.")
    private String confirmPassword;
}