package sports.center.com.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sports.center.com.dao.GenericDao;
import sports.center.com.model.Training;
import sports.center.com.service.TrainingService;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TrainingServiceImpl implements TrainingService {

    private final GenericDao<Training> trainingDao;

    @Autowired
    public TrainingServiceImpl(GenericDao<Training> trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public void create(Training training) {
        log.debug("Starting Training creation process. Input data: {}", training);
        trainingDao.create(training);
        log.info("Training successfully created with ID={}", training.getId());
    }

    @Override
    public Optional<Training> getById(long id) {
        log.debug("Fetching Training with ID={}", id);
        Optional<Training> training = trainingDao.findById(id);
        log.info("Fetched Training: {}", training.orElse(null));
        return training;
    }

    @Override
    public List<Training> getAll() {
        List<Training> trainings = trainingDao.findAll();
        return trainings;
    }
}