package sports.center.com.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Trainee extends User {
    @JsonAlias("birthDate")
    @JsonProperty("dateOfBirth")
    private LocalDate dateOfBirth;
    @JsonProperty("address")
    private String address;

    public Trainee(String firstName, String lastName, String username, String password, boolean isActive,
                   LocalDate dateOfBirth, String address) {
        super(firstName, lastName, username, password, isActive);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                "} " + super.toString();
    }
}