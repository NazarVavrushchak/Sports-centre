package sports.center.com.dto.training;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainingResponseDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String traineeUsername;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String trainerUsername;
    private String trainingName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date trainingDate;
    private int trainingDuration;
    private String trainingTypeName;
}