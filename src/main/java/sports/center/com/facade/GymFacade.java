package sports.center.com.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sports.center.com.dao.GenericDao;
import sports.center.com.dao.TraineeDao;
import sports.center.com.dao.TrainerDao;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;
import sports.center.com.service.TrainingService;

import java.util.List;
import java.util.Optional;

@Component
public class GymFacade {
    private final TrainerService trainerService;
    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final TrainerDao trainerDao;
    private final TraineeDao traineeDao;

    @Autowired
    public GymFacade(
            TrainerService trainerService,
            TraineeService traineeService,
            TrainingService trainingService,
            TrainerDao trainerDao,
            TraineeDao traineeDao) {
        this.trainerService = trainerService;
        this.traineeService = traineeService;
        this.trainingService = trainingService;
        this.trainerDao = trainerDao;
        this.traineeDao = traineeDao;
    }

    public void createTrainee(Trainee trainee) {
        traineeService.create(trainee);
    }

    public void updateTrainee(long id, Trainee trainee) {
        traineeService.updateTrainee(id, trainee);
    }

    public void deleteTrainee(long id) {
        traineeService.deleteTrainee(id);
    }

    public Optional<Trainee> getTraineeById(long id) {
        return traineeService.getById(id);
    }

    public List<Trainee> getAllTrainees() {
        return traineeService.getAll();
    }

    public void createTrainer(Trainer trainer) {
        trainerService.create(trainer);
    }

    public void updateTrainer(long id, Trainer trainer) {
        trainerService.update(id, trainer);
    }

    public void deleteTrainer(long id) {
        trainerService.deleteTrainer(id);
    }

    public Optional<Trainer> getTrainerById(long id) {
        return trainerService.getById(id);
    }

    public List<Trainer> getAllTrainers() {
        return trainerService.getAll();
    }

    public void createTraining(Training training) {
        trainingService.create(training);
    }

    public Optional<Training> getTrainingById(long id) {
        return trainingService.getById(id);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAll();
    }

    public GenericDao<Trainee> getTraineeDao() {
        return traineeDao;
    }

    public GenericDao<Trainer> getTrainerDao() {
        return trainerDao;
    }
}