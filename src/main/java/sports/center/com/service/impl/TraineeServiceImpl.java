package sports.center.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Trainee;
import sports.center.com.service.TraineeService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TraineeServiceImpl implements TraineeService {
    private final GenericDao<Trainee> traineeDao;

    @Autowired
    public TraineeServiceImpl(GenericDao<Trainee> traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Override
    public void create(Trainee trainee) {
        log.debug("Starting Trainee creation process. Input data: {}", trainee);

        Set<String> existingUsernames = getAllUsernames();
        String username = UsernameUtil.generateUsername(trainee.getFirstName(), trainee.getLastName(), existingUsernames);
        trainee.setUsername(username);

        trainee.setPassword(PasswordUtil.generatePassword());

        traineeDao.create(trainee);
        log.info("Trainee created successfully. Username: {}, ID: {}", username, trainee.getId());
    }

    @Override
    public void update(long id, Trainee newTraineeData) {
        log.debug("Updating Trainee with ID={}. New data: {}", id, newTraineeData);

        Trainee existingTrainee = getTraineeOrThrow(id);
        updateTraineeFields(existingTrainee, newTraineeData);

        traineeDao.update(id, existingTrainee);
        log.info("Trainee with ID={} successfully updated.", id);
    }

    private Trainee getTraineeOrThrow(long id) {
        return getById(id).orElseThrow(() -> {
            log.error("Trainee with ID={} not found.", id);
            return new IllegalArgumentException("Trainee not found with ID: " + id);
        });
    }

    private void updateTraineeFields(Trainee existingTrainee, Trainee newTraineeData) {
        Optional.ofNullable(newTraineeData.getFirstName()).ifPresent(existingTrainee::setFirstName);
        Optional.ofNullable(newTraineeData.getLastName()).ifPresent(existingTrainee::setLastName);
        Optional.ofNullable(newTraineeData.getDateOfBirth()).ifPresent(existingTrainee::setDateOfBirth);
        Optional.ofNullable(newTraineeData.getAddress()).ifPresent(existingTrainee::setAddress);

        Optional.ofNullable(newTraineeData.getUsername())
                .ifPresentOrElse(existingTrainee::setUsername,
                        () -> existingTrainee.setUsername(generateOrUpdateUsername(existingTrainee, newTraineeData)));

        Optional.ofNullable(newTraineeData.getPassword())
                .ifPresentOrElse(existingTrainee::setPassword,
                        () -> existingTrainee.setPassword(PasswordUtil.generatePassword()));

        existingTrainee.setActive(newTraineeData.isActive());
    }

    private String generateOrUpdateUsername(Trainee existingTrainee, Trainee newTraineeData) {
        Set<String> existingUsernames = getAllUsernames();
        return UsernameUtil.generateUsername(
                Optional.ofNullable(newTraineeData.getFirstName()).orElse(existingTrainee.getFirstName()),
                Optional.ofNullable(newTraineeData.getLastName()).orElse(existingTrainee.getLastName()),
                existingUsernames
        );
    }

    private Set<String> getAllUsernames() {
        return traineeDao.findAll().stream()
                .map(Trainee::getUsername)
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(long id) {
        log.debug("Deleting Trainee with ID={}", id);
        traineeDao.delete(id);
        log.info("Trainee with ID={} successfully deleted.", id);
    }

    @Override
    public Optional<Trainee> getById(long id) {
        log.debug("Fetching Trainee with ID={}", id);
        Optional<Trainee> trainee = traineeDao.findById(id);
        log.info("Fetched Trainee: {}", trainee.orElse(null));
        return trainee;
    }

    @Override
    public List<Trainee> getAll() {
        List<Trainee> trainees = traineeDao.findAll();
        return trainees;
    }
}