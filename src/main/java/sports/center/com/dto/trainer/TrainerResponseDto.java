package sports.center.com.dto.trainer;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TrainerResponseDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;
    private Long specializationId;
}