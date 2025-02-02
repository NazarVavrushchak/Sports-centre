package sports.center.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Trainer;
import sports.center.com.service.TrainerService;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrainerServiceImpl implements TrainerService {

    private final GenericDao<Trainer> trainerDao;

    @Autowired
    public TrainerServiceImpl(GenericDao<Trainer> trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Override
    public void create(Trainer trainer) {
        log.debug("Starting Trainer creation process. Input data: {}", trainer);

        Set<String> existingUsernames = getAllUsernames();
        String username = UsernameUtil.generateUsername(trainer.getFirstName(), trainer.getLastName(), existingUsernames);
        trainer.setUsername(username);

        trainer.setPassword(PasswordUtil.generatePassword());

        trainerDao.create(trainer);
        log.info("Trainer successfully created with Username={} and ID={}", username, trainer.getId());
    }

    @Override
    public void update(long id, Trainer newTrainerData) {
        log.debug("Updating Trainer with ID={}. New data: {}", id, newTrainerData);

        Trainer existingTrainer = getTrainerOrThrow(id);
        updateTrainerFields(existingTrainer, newTrainerData);

        trainerDao.update(id, existingTrainer);
        log.info("Trainer with ID={} successfully updated.", id);
    }

    private Trainer getTrainerOrThrow(long id) {
        return getById(id).orElseThrow(() -> {
            log.error("Trainer with ID={} not found.", id);
            return new IllegalArgumentException("Trainer not found with ID: " + id);
        });
    }

    private void updateTrainerFields(Trainer existingTrainer, Trainer newTrainerData) {
        Optional.ofNullable(newTrainerData.getFirstName()).ifPresent(existingTrainer::setFirstName);
        Optional.ofNullable(newTrainerData.getLastName()).ifPresent(existingTrainer::setLastName);
        Optional.ofNullable(newTrainerData.getSpecialization()).ifPresent(existingTrainer::setSpecialization);
        Optional.ofNullable(newTrainerData.getUsername())
                .ifPresentOrElse(existingTrainer::setUsername,
                        () -> existingTrainer.setUsername(generateOrUpdateUsername(existingTrainer, newTrainerData)));

        Optional.ofNullable(newTrainerData.getPassword())
                .ifPresentOrElse(existingTrainer::setPassword,
                        () -> existingTrainer.setPassword(PasswordUtil.generatePassword()));

        existingTrainer.setActive(newTrainerData.isActive());
    }

    private String generateOrUpdateUsername(Trainer existingTrainee, Trainer newTraineeData) {
        Set<String> existingUsernames = getAllUsernames();
        return UsernameUtil.generateUsername(
                Optional.ofNullable(newTraineeData.getFirstName()).orElse(existingTrainee.getFirstName()),
                Optional.ofNullable(newTraineeData.getLastName()).orElse(existingTrainee.getLastName()),
                existingUsernames
        );
    }

    private Set<String> getAllUsernames() {
        return trainerDao.findAll().stream()
                .map(Trainer::getUsername)
                .collect(Collectors.toSet());
    }

    @Override
    public Optional<Trainer> getById(long id) {
        log.debug("Fetching Trainer with ID={}", id);
        Optional<Trainer> trainer = trainerDao.findById(id);
        log.info("Fetched Trainer: {}", trainer.orElse(null));
        return trainer;
    }

    @Override
    public List<Trainer> getAll() {
        return trainerDao.findAll();
    }

    @Override
    public void delete(long id) {
        log.debug("Deleting Trainer with ID={}", id);
        trainerDao.delete(id);
        log.info("Trainer with ID={} successfully deleted.", id);
    }
}