package sports.center.com.dto.trainee;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraineeResponseDto {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Date dateOfBirth;
    private String address;
    private boolean isActive;
}