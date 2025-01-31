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

        String username = UsernameUtil.generateUsername(trainer.getFirstName(), trainer.getLastName(), trainerDao);
        trainer.setUsername(username);

        String password = PasswordUtil.generatePassword();
        trainer.setPassword(password);

        trainerDao.create(trainer);

        log.info("Trainer successfully created with Username={} and ID={}", username, trainer.getId());
    }

    @Override
    public void update(long id, Trainer newTrainerData) {
        log.debug("Updating Trainer with ID={}. New data: {}", id, newTrainerData);

        Optional<Trainer> existingTrainerOpt = trainerDao.findById(id);
        if (existingTrainerOpt.isEmpty()) {
            log.error("Trainer with ID={} not found.", id);
            throw new IllegalArgumentException("Trainer not found with ID: " + id);
        }

        Trainer existingTrainer = existingTrainerOpt.get();

        if (newTrainerData.getFirstName() != null) {
            existingTrainer.setFirstName(newTrainerData.getFirstName());
        }
        if (newTrainerData.getLastName() != null) {
            existingTrainer.setLastName(newTrainerData.getLastName());
        }
        if (newTrainerData.getSpecialization() != null) {
            existingTrainer.setSpecialization(newTrainerData.getSpecialization());
        }
        if (newTrainerData.getUsername() != null) {
            existingTrainer.setUsername(newTrainerData.getUsername());
        } else {
            String username = UsernameUtil.generateUsername(
                    newTrainerData.getFirstName() != null ? newTrainerData.getFirstName() : existingTrainer.getFirstName(),
                    newTrainerData.getLastName() != null ? newTrainerData.getLastName() : existingTrainer.getLastName(),
                    trainerDao
            );
            existingTrainer.setUsername(username);
        }

        if (newTrainerData.getPassword() != null) {
            existingTrainer.setPassword(newTrainerData.getPassword());
        } else {
            existingTrainer.setPassword(PasswordUtil.generatePassword());
        }

        existingTrainer.setActive(newTrainerData.isActive());

        trainerDao.update(id, existingTrainer);
        log.info("Trainer with ID={} successfully updated.", id);
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
        List<Trainer> trainers = trainerDao.findAll();
        return trainers;
    }

    @Override
    public void deleteTrainer(long id) {
        log.debug("Deleting Trainer with ID={}", id);
        trainerDao.delete(id);
        log.info("Trainer with ID={} successfully deleted.", id);
    }
}