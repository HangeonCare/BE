package KEPCO.SSD.user.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhoneNumber;

    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            if (toPhoneNumber.startsWith("010")) {
                toPhoneNumber = toPhoneNumber.replaceFirst("010", "+8210");
            }
            Twilio.init(accountSid, authToken);
            Message.creator(
                            new PhoneNumber(toPhoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            messageBody)
                    .create();
        } catch (Exception e) {
            throw new RuntimeException("SMS 전송에 실패했습니다.", e);
        }
    }
}