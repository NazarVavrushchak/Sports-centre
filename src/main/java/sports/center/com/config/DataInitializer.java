package sports.center.com.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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

    @Value("${data.initial.trainers.file}")
    private String trainersFile;

    @Value("${data.initial.trainees.file}")
    private String traineesFile;

    @Value("${data.initial.trainings.file}")
    private String trainingsFile;

    private final InMemoryStorage storage;
    private final ObjectMapper objectMapper;

    public DataInitializer(InMemoryStorage storage, ObjectMapper objectMapper) {
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void fillInData() {
        try {
            storage.loadInitialData("trainee", loadDataFromJson(traineesFile, new TypeReference<List<Trainee>>() {
            }));
            storage.loadInitialData("trainer", loadDataFromJson(trainersFile, new TypeReference<List<Trainer>>() {
            }));
            storage.loadInitialData("training", loadDataFromJson(trainingsFile, new TypeReference<List<Training>>() {
            }));

            log.info("Data initialization completed successfully.");
        } catch (IOException e) {
            log.error("Failed to load initial data: {}", e.getMessage(), e);
            throw new RuntimeException("Data initialization failed", e);
        }
    }

    private <T> List<T> loadDataFromJson(String filePath, TypeReference<List<T>> typeReference) throws IOException {
        return objectMapper.readValue(
                getClass().getClassLoader().getResourceAsStream(filePath.replace("classpath:", "")),
                typeReference
        );
    }
}