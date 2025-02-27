package sports.center.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.service.AuthService;
import sports.center.com.service.TrainerService;

@Slf4j
@RestController
@RequestMapping("/trainer")
@RequiredArgsConstructor
@Tag(name = "Trainer Management", description = "Operations related to trainers")
public class TrainerController {
    private final TrainerService trainerService;
    private final AuthService authService;

    @Operation(summary = "Register new trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
            @ApiResponse(responseCode = "303", description = HttpStatuses.SEE_OTHER),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PostMapping
    public ResponseEntity<TrainerResponseDto> registerTrainer(@Valid @RequestBody TrainerRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.createTrainer(request));
    }

    @Operation(summary = "Login trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/login")
    public ResponseEntity<String> login(HttpServletRequest request) {
        if (!authService.authenticateRequest(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(HttpStatuses.UNAUTHORIZED);
        }
        return ResponseEntity.ok(HttpStatuses.OK);
    }

    @Operation(summary = "Activate/De-Activate Trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PatchMapping("/status")
    public ResponseEntity<String> toggleTrainerStatus() {
        boolean newStatus = trainerService.changeTrainerStatus();
        return ResponseEntity.ok("Trainer status changed to: " + newStatus);
    }

    @Operation(summary = "Get trainer profile by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @GetMapping("/username")
    public ResponseEntity<TrainerResponseDto> getTrainerProfile() {
        return ResponseEntity.status(HttpStatus.OK).body(trainerService.getTrainerProfile());
    }

    @Operation(summary = "Update trainer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PutMapping("/username")
    public ResponseEntity<TrainerResponseDto> updateTrainerProfile(@Valid @RequestBody TrainerRequestDto request) {
        return ResponseEntity.status(HttpStatus.OK).body(trainerService.updateTrainerProfile(request));
    }
}