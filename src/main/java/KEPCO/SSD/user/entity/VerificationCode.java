package KEPCO.SSD.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    private String verificationCode;
    private String phoneNumber;
    private LocalDateTime sentAt;
}