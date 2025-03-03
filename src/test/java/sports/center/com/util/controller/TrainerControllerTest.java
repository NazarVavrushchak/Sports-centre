package sports.center.com.util.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sports.center.com.controller.TrainerController;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.service.AuthService;
import sports.center.com.service.TrainerService;
import sports.center.com.service.TransactionService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainerService trainerService;

    @Mock
    private AuthService authService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TrainerController trainerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainerController).build();
    }

    @Test
    void shouldRegisterTrainer() throws Exception {
        TrainerRequestDto requestDto = new TrainerRequestDto("John", "Doe", 1L, true);
        TrainerResponseDto responseDto = TrainerResponseDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .password("password123")
                .specializationId(1L)
                .specializationName("Fitness")
                .isActive(true)
                .trainees(Collections.emptyList())
                .build();

        when(trainerService.createTrainer(any(TrainerRequestDto.class))).thenReturn(responseDto);

        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"specializationId\":1,\"isActive\":true}";

        mockMvc.perform(post("/trainer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldLoginTrainer() throws Exception {
        when(authService.authenticateRequest(any())).thenReturn(true);

        mockMvc.perform(get("/trainer/login"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailLoginTrainer() throws Exception {
        when(authService.authenticateRequest(any())).thenReturn(false);

        mockMvc.perform(get("/trainer/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldToggleTrainerStatus() throws Exception {
        when(trainerService.changeTrainerStatus()).thenReturn(true);

        mockMvc.perform(patch("/trainer/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trainer status changed to: true"));

        when(trainerService.changeTrainerStatus()).thenReturn(false);

        mockMvc.perform(patch("/trainer/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trainer status changed to: false"));
    }


    @Test
    void shouldGetTrainerProfile() throws Exception {
        TrainerResponseDto responseDto = TrainerResponseDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .specializationId(1L)
                .specializationName("Fitness")
                .isActive(true)
                .trainees(Collections.emptyList())
                .build();

        when(trainerService.getTrainerProfile()).thenReturn(responseDto);

        mockMvc.perform(get("/trainer/username"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTrainerProfile() throws Exception {
        TrainerRequestDto requestDto = new TrainerRequestDto("Updated", "Trainer", 2L, true);
        TrainerResponseDto responseDto = TrainerResponseDto.builder()
                .firstName("Updated")
                .lastName("Trainer")
                .username("updatedtrainer")
                .specializationId(2L)
                .specializationName("Strength Training")
                .isActive(true)
                .trainees(Collections.emptyList())
                .build();

        when(trainerService.updateTrainerProfile(any(TrainerRequestDto.class))).thenReturn(responseDto);

        String requestBody = "{\"firstName\":\"Updated\",\"lastName\":\"Trainer\",\"specializationId\":2,\"isActive\":true}";

        mockMvc.perform(put("/trainer/username")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }
}
