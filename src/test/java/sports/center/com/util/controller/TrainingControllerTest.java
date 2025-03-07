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
import sports.center.com.controller.TrainingController;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.dto.training.TrainingTypeResponseDto;
import sports.center.com.service.TrainingService;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trainingController).build();
    }

    @Test
    void shouldGetNotAssignedActiveTrainers() throws Exception {
        List<TrainerResponseDto> trainers = Collections.singletonList(
                TrainerResponseDto.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .username("johndoe")
                        .isActive(true)
                        .build()
        );

        when(trainingService.getNotAssignedActiveTrainers()).thenReturn(trainers);

        mockMvc.perform(get("/training/not-assigned"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTraineeTrainers() throws Exception {
        List<String> trainerUsernames = List.of("trainer1", "trainer2");

        List<TrainerResponseDto> updatedTrainers = List.of(
                TrainerResponseDto.builder().username("trainer1").build(),
                TrainerResponseDto.builder().username("trainer2").build()
        );

        when(trainingService.updateTraineeTrainersList(anyList())).thenReturn(updatedTrainers);

        mockMvc.perform(put("/training/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"trainer1\", \"trainer2\"]"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTraineeTrainings() throws Exception {
        List<TrainingResponseDto> trainings = List.of(
                TrainingResponseDto.builder()
                        .traineeUsername("trainee1")
                        .trainerUsername("trainer1")
                        .trainingName("Cardio")
                        .trainingDate(new Date())
                        .trainingDuration(30)
                        .trainingTypeName("Cardio")
                        .build()
        );

        when(trainingService.getTraineeTrainings(any(), any(), any(), any())).thenReturn(trainings);

        mockMvc.perform(get("/training/trainee")
                        .param("fromDate", "2025-02-01")
                        .param("toDate", "2025-03-01")
                        .param("trainerName", "trainer1")
                        .param("trainingType", "Cardio"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTrainerTrainings() throws Exception {
        List<TrainingResponseDto> trainings = List.of(
                TrainingResponseDto.builder()
                        .traineeUsername("trainee1")
                        .trainerUsername("trainer1")
                        .trainingName("Cardio")
                        .trainingDate(new Date())
                        .trainingDuration(30)
                        .trainingTypeName("Cardio")
                        .build()
        );

        when(trainingService.getTrainerTrainings(any(), any(), any())).thenReturn(trainings);

        mockMvc.perform(get("/training/trainer")
                        .param("fromDate", "2025-02-01")
                        .param("toDate", "2025-03-01")
                        .param("trainerName", "trainee1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetTrainingTypes() throws Exception {
        List<TrainingTypeResponseDto> trainingTypes = List.of(
                new TrainingTypeResponseDto(1L, "Cardio"),
                new TrainingTypeResponseDto(2L, "Strength")
        );

        when(trainingService.getTrainingType()).thenReturn(trainingTypes);

        mockMvc.perform(get("/training/training-types"))
                .andExpect(status().isOk());
    }
}
