package KEPCO.SSD.user.repository;

import KEPCO.SSD.user.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
    VerificationCode findByPhoneNumber(String phoneNumber);
}