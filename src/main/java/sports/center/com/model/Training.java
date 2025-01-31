package sports.center.com.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sports.center.com.enumeration.TrainingType;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class Training {
    private long id;
    private long traineeId;
    private long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private int trainingDuration;
}