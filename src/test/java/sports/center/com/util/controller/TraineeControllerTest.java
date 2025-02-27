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
import sports.center.com.controller.TraineeController;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.service.AuthService;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TransactionService;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TraineeService traineeService;

    @Mock
    private AuthService authService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TraineeController traineeController;

    private String validDateOfBirth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(traineeController).build();
        validDateOfBirth = new SimpleDateFormat("yyyy-MM-dd").format(new Date(90, 0, 1));
    }

    @Test
    void shouldRegisterTrainee() throws Exception {
        TraineeRequestDto requestDto = new TraineeRequestDto("John", "Doe", new Date(90, 0, 1), "123 Main St", true);
        TraineeResponseDto responseDto = TraineeResponseDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .password("password123")
                .dateOfBirth(new Date(90, 0, 1))
                .address("123 Main St")
                .isActive(true)
                .trainers(Collections.emptyList())
                .build();

        when(traineeService.createTrainee(any(TraineeRequestDto.class))).thenReturn(responseDto);

        String requestBody = String.format(
                "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"password\":\"password123\",\"dateOfBirth\":\"%s\",\"address\":\"123 Main St\",\"isActive\":true}",
                validDateOfBirth
        );

        mockMvc.perform(post("/trainee")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldLoginTrainee() throws Exception {
        when(authService.authenticateRequest(any())).thenReturn(true);

        mockMvc.perform(get("/trainee/login"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailLoginTrainee() throws Exception {
        when(authService.authenticateRequest(any())).thenReturn(false);

        mockMvc.perform(get("/trainee/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldChangeLogin() throws Exception {
        when(traineeService.changeTraineePassword(any())).thenReturn(true);

        mockMvc.perform(put("/trainee/change-login?newPassword=newpass"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTraineeProfile() throws Exception {
        TraineeResponseDto responseDto = TraineeResponseDto.builder()
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .dateOfBirth(new Date(90, 0, 1))
                .address("123 Main St")
                .isActive(true)
                .trainers(Collections.emptyList())
                .build();

        when(traineeService.getTraineeProfile()).thenReturn(responseDto);

        mockMvc.perform(get("/trainee/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTraineeProfile() throws Exception {
        TraineeRequestDto requestDto = new TraineeRequestDto("Updated", "User", new Date(90, 0, 1), "456 New St", true);
        TraineeResponseDto responseDto = TraineeResponseDto.builder()
                .firstName("Updated")
                .lastName("User")
                .username("updateduser")
                .dateOfBirth(new Date(90, 0, 1))
                .address("456 New St")
                .isActive(true)
                .trainers(Collections.emptyList())
                .build();

        when(traineeService.updateTraineeProfile(any(TraineeRequestDto.class))).thenReturn(responseDto);

        String requestBody = String.format(
                "{\"firstName\":\"Updated\",\"lastName\":\"User\",\"dateOfBirth\":\"%s\",\"address\":\"456 New St\",\"isActive\":true}",
                validDateOfBirth
        );

        mockMvc.perform(put("/trainee/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteTraineeProfile() throws Exception {
        when(traineeService.deleteTrainee()).thenReturn(true);

        mockMvc.perform(delete("/trainee/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldToggleTraineeStatus() throws Exception {
        when(traineeService.changeTraineeStatus()).thenReturn(true);

        mockMvc.perform(patch("/trainee/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trainee status changed to: true"));

        when(traineeService.changeTraineeStatus()).thenReturn(false);

        mockMvc.perform(patch("/trainee/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Trainee status changed to: false"));
    }

}
