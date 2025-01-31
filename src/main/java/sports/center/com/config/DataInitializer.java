package sports.center.com.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.storage.InMemoryStorage;

import java.io.IOException;
import java.util.List;


@Component
@Slf4j
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${data.initial.trainers.file}")
    private String trainersFile;

    @Value("${data.initial.trainees.file}")
    private String traineesFile;

    @Value("${data.initial.trainings.file}")
    private String trainingsFile;

    private final InMemoryStorage storage;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataInitializer(InMemoryStorage storage, ObjectMapper objectMapper) {
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void fillInData() {
        try {
            List<Trainee> trainees = loadTraineesFromJson(traineesFile);
            List<Trainer> trainers = loadTrainersFromJson(trainersFile);
            List<Training> trainings = loadTrainingsFromJson(trainingsFile);

            storage.loadInitialData("trainee", trainees);
            storage.loadInitialData("trainer", trainers);
            storage.loadInitialData("training", trainings);

            logger.info("Data initialization from JSON completed successfully.");
        } catch (IOException e) {
            logger.error("Failed to load initial data: {}", e.getMessage(), e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }

    private List<Trainee> loadTraineesFromJson(String filePath) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(filePath.replace("classpath:", "")),
                new TypeReference<>() {
                }
        );
    }

    private List<Trainer> loadTrainersFromJson(String filePath) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(filePath.replace("classpath:", "")),
                new TypeReference<>() {
                }
        );
    }

    private List<Training> loadTrainingsFromJson(String filePath) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(filePath.replace("classpath:", "")),
                new TypeReference<>() {
                }
        );
    }
}