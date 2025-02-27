package sports.center.com.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraineeRequestDto {
    @NotBlank(message = "First name is mandatory")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters long")
    private String firstName;
    @NotBlank(message = "Last name is mandatory")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters long")
    private String lastName;
    @Past(message = "Date of birth must be in the past")
    private Date dateOfBirth;
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters long")
    private String address;
    private Boolean isActive;
}