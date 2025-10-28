package sports.center.com.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sports.center.com.repository.TraineeRepository;
import sports.center.com.repository.TrainerRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return traineeRepository.findByUsername(username)
                .map(trainee -> new User(trainee.getUsername(), trainee.getPassword(),
                        java.util.Collections.emptyList()))
                .or(() -> trainerRepository.findByUsername(username)
                        .map(trainer -> new User(trainer.getUsername(), trainer.getPassword(),
                                java.util.Collections.emptyList())))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}