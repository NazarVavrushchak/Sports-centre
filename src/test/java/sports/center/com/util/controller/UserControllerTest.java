package sports.center.com.util.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.controller.UserController;
import sports.center.com.service.AuthService;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void testLogin_Success() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        doReturn(true).when(authService).authenticateRequest(any(HttpServletRequest.class));

        mockMvc.perform(get("/users/login").requestAttr("mockRequest", request))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatuses.OK));
    }

    @Test
    void testLogin_Unauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        doReturn(false).when(authService).authenticateRequest(any(HttpServletRequest.class));

        mockMvc.perform(get("/users/login").requestAttr("mockRequest", request))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(HttpStatuses.UNAUTHORIZED));
    }

    @Test
    void testChangeTraineeLogin_Success() throws Exception {
        when(traineeService.changeTraineePassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatuses.OK));
    }

    @Test
    void testChangeTraineeLogin_Failure() throws Exception {
        when(traineeService.changeTraineePassword("newPassword")).thenReturn(false);

        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(HttpStatuses.BAD_REQUEST));
    }

    @Test
    void testChangeTrainerLogin_Success() throws Exception {
        when(trainerService.changeTrainerPassword("newPassword")).thenReturn(true);

        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(HttpStatuses.OK));
    }

    @Test
    void testChangeTrainerLogin_Failure() throws Exception {
        when(trainerService.changeTrainerPassword("newPassword")).thenReturn(false);

        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "newPassword")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(HttpStatuses.BAD_REQUEST));
    }

    @Test
    void testLogin_MissingRequest() throws Exception {
        mockMvc.perform(get("/users/login"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangeTraineeLogin_MissingPassword() throws Exception {
        mockMvc.perform(put("/users/trainee/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangeTraineeLogin_EmptyPassword() throws Exception {
        mockMvc.perform(put("/users/trainee/login")
                        .param("newPassword", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangeTrainerLogin_MissingPassword() throws Exception {
        mockMvc.perform(put("/users/trainer/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testChangeTrainerLogin_EmptyPassword() throws Exception {
        mockMvc.perform(put("/users/trainer/login")
                        .param("newPassword", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_InvalidMethod() throws Exception {
        mockMvc.perform(put("/users/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testChangeTraineeLogin_InvalidMethod() throws Exception {
        mockMvc.perform(get("/users/trainee/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void testChangeTrainerLogin_InvalidMethod() throws Exception {
        mockMvc.perform(get("/users/trainer/login"))
                .andExpect(status().isMethodNotAllowed());
    }
}