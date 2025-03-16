package sports.center.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sports.center.com.constant.HttpStatuses;
import sports.center.com.dto.security.AuthRequest;
import sports.center.com.dto.security.AuthResponse;
import sports.center.com.service.AuthService;
import sports.center.com.service.LogoutService;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final AuthService authService;
    private final LogoutService logoutService;

    @Operation(summary = "Log in a user", description = "Authenticate a user and return a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials")
    })
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        return authService.authenticateAndGenerateToken(authRequest.getUsername(), authRequest.getPassword(), ipAddress);
    }

    @Operation(summary = "Log out a user", description = "Invalidate the user's JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid or missing token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - User not authenticated")
    })
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logout(HttpServletRequest request) {
        return logoutService.logout(request);
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