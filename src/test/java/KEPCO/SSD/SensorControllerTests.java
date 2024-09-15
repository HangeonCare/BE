package KEPCO.SSD;
import KEPCO.SSD.sensor.controller.SensorController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(SensorController.class)
public class SensorControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetDevices() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}