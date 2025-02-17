package sports.center.com.dto.training;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainingResponseDto {
    private String traineeName;
    private String trainerName;
    private String trainingType;
    private String trainingName;
    private Date trainingDate;
    private int trainingDuration;
}