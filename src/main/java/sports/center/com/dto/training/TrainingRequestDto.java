package sports.center.com.dto.training;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainingRequestDto {
    @NotBlank(message = "Trainee username is required")
    private String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    private String trainerUsername;

    @NotBlank(message = "Training name is mandatory")
    @Size(min = 5, max = 100, message = "Training name must be between 5 and 100 characters long")
    private String trainingName;

    @NotNull(message = "Training date is required")
    @FutureOrPresent(message = "Training date must be in the present or future")
    private Date trainingDate;

    @NotNull(message = "Training duration is required")
    @Min(value = 1, message = "Training duration must be at least 1 minute")
    private Integer trainingDuration;

    @NotBlank(message = "Training type is required")
    private String trainingTypeName;
}
