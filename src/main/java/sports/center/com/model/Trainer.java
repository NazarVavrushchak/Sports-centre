package sports.center.com.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "trainers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trainer extends User {
    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @ManyToMany(mappedBy = "trainers")
    private List<Trainee> trainees;

    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings;

    public Trainer(String firstName, String lastName, String username, String password, Boolean isActive, TrainingType specialization) {
        super();
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setUsername(username);
        this.setPassword(password);
        this.setIsActive(isActive);
        this.specialization = specialization;
    }
}