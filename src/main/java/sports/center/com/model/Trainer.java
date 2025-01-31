package sports.center.com.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Trainer extends User {
    private String specialization;

    public Trainer(String firstName, String lastName, String username, String password, boolean isActive,
                   String specialization) {
        super(firstName, lastName, username, password, isActive);
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "specialization='" + specialization + '\'' +
                "} " + super.toString();
    }
}