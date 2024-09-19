package KEPCO.SSD.user.dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    private String password;
}