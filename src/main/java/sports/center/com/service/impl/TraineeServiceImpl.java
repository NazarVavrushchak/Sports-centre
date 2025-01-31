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

        String username = UsernameUtil.generateUsername(trainee.getFirstName(), trainee.getLastName(), traineeDao);
        trainee.setUsername(username);

        String password = PasswordUtil.generatePassword();
        trainee.setPassword(password);

        traineeDao.create(trainee);

        log.info("Trainee created successfully. Username: {}, ID: {}, Password: {}", username, trainee.getId(), password);
    }

    @Override
    public void updateTrainee(long id, Trainee newTraineeData) {
        log.debug("Updating Trainee with ID={}. New data: {}", id, newTraineeData);

        Optional<Trainee> existingTraineeOpt = traineeDao.findById(id);
        if (existingTraineeOpt.isEmpty()) {
            log.error("Trainee with ID={} not found.", id);
            throw new IllegalArgumentException("Trainee not found with ID: " + id);
        }

        Trainee existingTrainee = existingTraineeOpt.get();

        if (newTraineeData.getFirstName() != null) {
            existingTrainee.setFirstName(newTraineeData.getFirstName());
        }
        if (newTraineeData.getLastName() != null) {
            existingTrainee.setLastName(newTraineeData.getLastName());
        }
        if (newTraineeData.getDateOfBirth() != null) {
            existingTrainee.setDateOfBirth(newTraineeData.getDateOfBirth());
        }
        if (newTraineeData.getAddress() != null) {
            existingTrainee.setAddress(newTraineeData.getAddress());
        }
        if (newTraineeData.getUsername() != null) {
            existingTrainee.setUsername(newTraineeData.getUsername());
        } else {
            String username = UsernameUtil.generateUsername(
                    newTraineeData.getFirstName() != null ? newTraineeData.getFirstName() : existingTrainee.getFirstName(),
                    newTraineeData.getLastName() != null ? newTraineeData.getLastName() : existingTrainee.getLastName(),
                    traineeDao
            );
            existingTrainee.setUsername(username);
        }

        if (newTraineeData.getPassword() != null) {
            existingTrainee.setPassword(newTraineeData.getPassword());
        } else {
            existingTrainee.setPassword(PasswordUtil.generatePassword());
        }

        existingTrainee.setActive(newTraineeData.isActive());

        traineeDao.update(id, existingTrainee);
        log.info("Trainee with ID={} successfully updated.", id);
    }

    @Override
    public void deleteTrainee(long id) {
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