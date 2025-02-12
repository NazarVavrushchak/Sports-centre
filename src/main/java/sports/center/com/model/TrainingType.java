package sports.center.com.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "training_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "training_type_id")
    private Long id;

    @Column(nullable = false)
    private String trainingTypeName;

    @OneToMany(mappedBy = "specialization", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trainer> trainers;

    @OneToMany(mappedBy = "trainingType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings;
}