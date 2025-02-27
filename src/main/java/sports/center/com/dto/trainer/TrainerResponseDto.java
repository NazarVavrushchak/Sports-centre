package sports.center.com.dto.trainer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import sports.center.com.dto.trainee.TraineeResponseDto;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TrainerResponseDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;
    private Long specializationId;
    private String specializationName;

    @Singular
    List<TraineeResponseDto> trainees;
}