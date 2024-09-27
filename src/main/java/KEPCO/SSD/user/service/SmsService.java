package KEPCO.SSD.user.service;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.type.PhoneNumber;
import com.twilio.rest.api.v2010.account.Message;
import jakarta.annotation.PostConstruct;
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
            Twilio.init(accountSid, authToken);
            Message message = Message
                    .creator(
                            new PhoneNumber(toPhoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            messageBody
                    ).create();
            System.out.println("Sent message: " + message.getSid());
        } catch (ApiException e) {
            System.err.println("Failed to send SMS: " + e.getMessage());
        }
    }
    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    /*
    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
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
     */
}