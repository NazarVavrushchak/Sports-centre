package sports.center.com.util.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;
import sports.center.com.security.CustomUserDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private final String username = "testUser";
    private final String password = "testPassword";

    @BeforeEach
    void setUp() {
        Mockito.reset(traineeRepository, trainerRepository);
    }

    @Test
    void loadUserByUsername_WhenTraineeExists_ShouldReturnUserDetails() {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        trainee.setPassword(password);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.of(trainee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        verify(traineeRepository, times(1)).findByUsername(username);
        verify(trainerRepository, never()).findByUsername(anyString());
    }

    @Test
    void loadUserByUsername_WhenTrainerExists_ShouldReturnUserDetails() {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        trainer.setPassword(password);

        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.of(trainer));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        verify(traineeRepository, times(1)).findByUsername(username);
        verify(trainerRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        when(traineeRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(trainerRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(username));

        verify(traineeRepository, times(1)).findByUsername(username);
        verify(trainerRepository, times(1)).findByUsername(username);
    }
}

