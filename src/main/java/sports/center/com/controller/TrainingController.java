package sports.center.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.dto.training.TrainingTypeResponseDto;
import sports.center.com.service.TrainingService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/training")
@RequiredArgsConstructor
@Tag(name = "Training Management", description = "Operations related to trainings")
public class TrainingController {
    private final TrainingService trainingService;

    @Operation(summary = "Add a new training")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingRequestDto request) {
        trainingService.addTraining(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get not assigned active trainers for authenticated trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/not-assigned")
    public ResponseEntity<List<TrainerResponseDto>> getNotAssignedActiveTrainers() {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getNotAssignedActiveTrainers());
    }

    @Operation(summary = "Update Trainee Trainer's list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PutMapping("/trainers")
    public ResponseEntity<List<TrainerResponseDto>> updateTraineeTrainers(
            @RequestBody @NotEmpty(message = "Trainer list cannot be empty") List<String> trainerUsernames) {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.updateTraineeTrainersList(trainerUsernames));
    }

    @Operation(summary = "Update Trainee Trainer's list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/trainee")
    public ResponseEntity<List<TrainingResponseDto>> getTraineeTrainings(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(value = "trainerName", required = false) String trainerName,
            @RequestParam(value = "trainingType", required = false) String trainingType) {

        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getTraineeTrainings(fromDate, toDate
                , trainerName, trainingType));
    }

    @Operation(summary = "Update Trainee Trainer's list")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/trainer")
    public ResponseEntity<List<TrainingResponseDto>> getTrainerTrainings(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate,
            @RequestParam(value = "traineeName", required = false) String traineeName) {

        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getTrainerTrainings(fromDate, toDate
                , traineeName));
    }

    @GetMapping("/training-types")
    public ResponseEntity<List<TrainingTypeResponseDto>> getTrainingTypes() {
        return ResponseEntity.status(HttpStatus.OK).body(trainingService.getTrainingType());
    }
}