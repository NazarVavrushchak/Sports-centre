package sports.center.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.service.AuthService;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    @Operation(summary = "Login")
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

    @Operation(summary = "Change Trainee login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PutMapping("/trainee/login")
    public ResponseEntity<String> changeTraineeLogin(@RequestParam("newPassword") String newPassword) {
        boolean isUpdated = traineeService.changeTraineePassword(newPassword);
        return isUpdated
                ? ResponseEntity.ok(HttpStatuses.OK)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpStatuses.BAD_REQUEST);
    }

    @Operation(summary = "Change Trainer login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "500", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PutMapping("/trainer/login")
    public ResponseEntity<String> changeTrainerLogin(@RequestParam("newPassword") String newPassword) {
        boolean isUpdated = trainerService.changeTrainerPassword(newPassword);
        return isUpdated
                ? ResponseEntity.ok(HttpStatuses.OK)
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpStatuses.BAD_REQUEST);
    }
}