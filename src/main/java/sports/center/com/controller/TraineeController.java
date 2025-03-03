package sports.center.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.service.AuthService;
import sports.center.com.service.TraineeService;

@Slf4j
@RestController
@RequestMapping("/trainee")
@RequiredArgsConstructor
@Tag(name = "Trainee Management", description = "Operations related to trainees")
public class TraineeController {
    private final TraineeService traineeService;
    private final AuthService authService;

    @Operation(summary = "Register a new trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
            @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PostMapping
    public ResponseEntity<TraineeResponseDto> registerTrainee(@Valid @RequestBody TraineeRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(traineeService.createTrainee(request));
    }

    @Operation(summary = "Get trainee profile by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/username")
    public ResponseEntity<TraineeResponseDto> getTraineeProfile() {
        return ResponseEntity.status(HttpStatus.OK).body(traineeService.getTraineeProfile());
    }

    @Operation(summary = "Update trainee profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PutMapping("/username")
    public ResponseEntity<TraineeResponseDto> updateTraineeProfile(@Valid @RequestBody TraineeRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(traineeService.updateTraineeProfile(request));
    }

    @Operation(summary = "Delete trainee profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @DeleteMapping("/profile")
    public ResponseEntity<String> deleteTraineeProfile() {
        boolean isDeleted = traineeService.deleteTrainee();
        return isDeleted
                ? ResponseEntity.ok(HttpStatuses.OK)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpStatuses.BAD_REQUEST);
    }

    @Operation(summary = "Activate/De-Activate Trainee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PatchMapping("/status")
    public ResponseEntity<String> toggleTraineeStatus() {
        boolean newStatus = traineeService.changeTraineeStatus();
        return ResponseEntity.ok("Trainee status changed to: " + newStatus);
    }
}