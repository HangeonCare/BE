package KEPCO.SSD.user.dto.Request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String phoneNumber;
    private String password;
    private long userId;
}