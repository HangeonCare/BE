package KEPCO.SSD.user.dto.Response;

import lombok.Data;

@Data
public class SignupResponseDto {
    private String phoneNumber;
    private String message;

    public SignupResponseDto(String message, String phoneNumber) {
        this.message = message;
        this.phoneNumber = phoneNumber;
    }
}