package sports.center.com.dto.training;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTypeResponseDto {
    private Long id;
    private String trainingTypeName;
}