package sports.center.com.util.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sports.center.com.controller.UserController;
import sports.center.com.dto.security.AuthRequest;
import sports.center.com.dto.security.AuthResponse;
import sports.center.com.service.AuthService;
import sports.center.com.service.LogoutService;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private LogoutService logoutService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void login_Success_ShouldReturnJwtToken() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "1234567890");
        AuthResponse authResponse = new AuthResponse("jwt-token");
        when(authService.authenticateAndGenerateToken(eq("testUser"), eq("1234567890"), eq("127.0.0.1")))
                .thenReturn(ResponseEntity.ok(authResponse));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest))
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "wrongPass");
        when(authService.authenticateAndGenerateToken(eq("testUser"), eq("wrongPass"), eq("127.0.0.1")))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null)));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest))
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.token").isEmpty());
    }

    @Test
    void logout_Success_ShouldReturnSuccessMessage() throws Exception {
        String token = "jwt-token";
        when(logoutService.logout(any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok("{\"message\": \"Logout successful\"}"));

        mockMvc.perform(post("/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\": \"Logout successful\"}"));

        verify(logoutService, times(1)).logout(any(HttpServletRequest.class));
    }

    @Test
    void logout_NoToken_ShouldReturnBadRequest() throws Exception {
        when(logoutService.logout(any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"No token provided\"}"));

        mockMvc.perform(post("/users/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\": \"No token provided\"}"));

        verify(logoutService, times(1)).logout(any(HttpServletRequest.class));
    }

    @Test
    void logout_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        String token = "invalid-token";
        when(logoutService.logout(any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Invalid or expired token\"}"));

        mockMvc.perform(post("/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\": \"Invalid or expired token\"}"));

        verify(logoutService, times(1)).logout(any(HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testTrainee", roles = "TRAINEE")
    void changeTraineeLogin_Success_ShouldReturnOk() throws Exception {
        when(traineeService.changeTraineePassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @WithMockUser(username = "testTrainee", roles = "TRAINEE")
    void changeTraineeLogin_Failure_ShouldReturnBadRequest() throws Exception {
        when(traineeService.changeTraineePassword("newPassword")).thenReturn(false);

        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    void changeTraineeLogin_Unauthenticated_ShouldReturnOk() throws Exception {
        when(traineeService.changeTraineePassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @WithMockUser(username = "testTrainer", roles = "TRAINER")
    void changeTrainerLogin_Success_ShouldReturnOk() throws Exception {
        when(trainerService.changeTrainerPassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    @WithMockUser(username = "testTrainer", roles = "TRAINER")
    void changeTrainerLogin_Failure_ShouldReturnBadRequest() throws Exception {
        when(trainerService.changeTrainerPassword("newPassword")).thenReturn(false);

        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bad Request"));
    }

    @Test
    void changeTrainerLogin_Unauthenticated_ShouldReturnOk() throws Exception {
        when(trainerService.changeTrainerPassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    void login_InvalidMethod_ShouldReturnMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/users/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void changeTraineeLogin_MissingPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/users/trainee/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changeTrainerLogin_MissingPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/users/trainer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ThreeFailedAttempts_ShouldReturnUnauthorizedWithLockMessage() throws Exception {
        AuthRequest authRequest = new AuthRequest("testUser", "wrongPass");

        when(authService.authenticateAndGenerateToken(eq("testUser"), eq("wrongPass"), eq("127.0.0.1")))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null)));

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(authRequest))
                            .with(request -> {
                                request.setRemoteAddr("127.0.0.1");
                                return request;
                            }))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.token").isEmpty());
        }

        when(authService.authenticateAndGenerateToken(eq("testUser"), eq("wrongPass"), eq("127.0.0.1")))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(null)));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest))
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").isEmpty());
    }
}