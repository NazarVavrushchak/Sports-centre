package sports.center.com;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sports.center.com.config.AppConfig;
import sports.center.com.dto.trainee.TraineeRequestDto;
import sports.center.com.dto.trainee.TraineeResponseDto;
import sports.center.com.dto.trainer.TrainerRequestDto;
import sports.center.com.dto.trainer.TrainerResponseDto;
import sports.center.com.dto.training.TrainingRequestDto;
import sports.center.com.dto.training.TrainingResponseDto;
import sports.center.com.service.TraineeService;
import sports.center.com.service.TrainerService;
import sports.center.com.service.TrainingService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            TraineeService traineeService = context.getBean(TraineeService.class);
            TrainerService trainerService = context.getBean(TrainerService.class);
            TrainingService trainingService = context.getBean(TrainingService.class);

            while (true) {
                showMenu();
                System.out.print("\nYour choice: ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1 -> createTrainee(traineeService);
                    case 2 -> getTrainee(traineeService);
                    case 3 -> updateTrainee(traineeService);
                    case 4 -> changeTraineePassword(traineeService);
                    case 5 -> changeTraineeStatus(traineeService);
                    case 6 -> deleteTrainee(traineeService);
                    case 7 -> createTrainer(trainerService);
                    case 8 -> getTrainer(trainerService);
                    case 9 -> updateTrainer(trainerService);
                    case 10 -> changeTrainerPassword(trainerService);
                    case 11 -> changeTrainerStatus(trainerService);
                    case 12 -> deleteTrainer(trainerService);
                    case 13 -> addTraining(trainingService);
                    case 14 -> getTraineeTrainings(trainingService);
                    case 15 -> getTrainerTrainings(trainingService);
                    case 16 -> getUnassignedTrainers(trainingService);
                    case 17 -> updateTraineeTrainers(trainingService);
                    case 0 -> {
                        System.out.println("\nExiting...");
                        return;
                    }
                    default -> System.out.println("\nUnknown command! Try again.");
                }
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n============================");
        System.out.println("SPORTS CENTER MANAGEMENT");
        System.out.println("============================");
        System.out.println("1 - Create Trainee");
        System.out.println("2 - Get Trainee Profile");
        System.out.println("3 - Update Trainee");
        System.out.println("4 - Change Trainee Password");
        System.out.println("5 - Activate/Deactivate Trainee");
        System.out.println("6 - Delete Trainee");
        System.out.println("7 - Create Trainer");
        System.out.println("8 - Get Trainer Profile");
        System.out.println("9 - Update Trainer");
        System.out.println("10 - Change Trainer Password");
        System.out.println("11 - Activate/Deactivate Trainer");
        System.out.println("12 - Delete Trainer");
        System.out.println("13 - Add Training");
        System.out.println("14 - Get Trainee Trainings");
        System.out.println("15 - Get Trainer Trainings");
        System.out.println("16 - Get Unassigned Trainers");
        System.out.println("17 - Update Trainee's Trainers");
        System.out.println("0 - Exit");
        System.out.println("============================");
    }

    private static void createTrainee(TraineeService traineeService) {
        System.out.println("\nCreating new trainee...");
        String firstName = getInput("First Name: ");
        String lastName = getInput("Last Name: ");
        Date dateOfBirth = getDateInput("Date of Birth (yyyy-MM-dd): ");
        String address = getInput("Address: ");

        TraineeRequestDto request = new TraineeRequestDto(firstName, lastName, dateOfBirth, address);
        TraineeResponseDto response = traineeService.createTrainee(request);

        System.out.println("\nTrainee created successfully!");
        printTrainee(response);
    }

    private static void getTrainee(TraineeService traineeService) {
        System.out.println("\nFetching trainee profile...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        try {
            TraineeResponseDto trainee = traineeService.getTraineeByUsername(username, password);
            printTrainee(trainee);
        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }
    }

    private static void updateTrainee(TraineeService traineeService) {
        System.out.println("\nUpdating trainee profile...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        String firstName = getInput("New First Name (or press Enter to skip): ");
        String lastName = getInput("New Last Name (or press Enter to skip): ");
        Date dateOfBirth = getDateInput("New Date of Birth (yyyy-MM-dd, or press Enter to skip): ");
        String address = getInput("New Address (or press Enter to skip): ");
        String newPassword = getInput("New Password (or press Enter to skip): ");

        TraineeRequestDto request = new TraineeRequestDto(
                firstName.isEmpty() ? null : firstName,
                lastName.isEmpty() ? null : lastName,
                dateOfBirth,
                address.isEmpty() ? null : address
        );

        boolean updated = traineeService.updateTrainee(username, password, request, newPassword.isEmpty() ? null : newPassword);
        System.out.println(updated ? "\nProfile updated successfully!" : "\nFailed to update profile.");
    }

    private static void changeTraineePassword(TraineeService traineeService) {
        System.out.println("\nChanging trainee password...");
        String username = getInput("Username: ");
        String oldPassword = getInput("Old Password: ");
        String newPassword = getInput("New Password: ");

        boolean success = traineeService.changeTraineePassword(username, oldPassword, newPassword);
        System.out.println(success ? "\nPassword changed successfully!" : "\nFailed to change password.");
    }

    private static void changeTraineeStatus(TraineeService traineeService) {
        System.out.println("\nToggling trainee status...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        boolean isActive = traineeService.changeTraineeStatus(username, password);
        System.out.println("\nTrainee status successfully changed! New status: " + (isActive ? "Active" : "Inactive"));
    }


    private static void deleteTrainee(TraineeService traineeService) {
        System.out.println("\nDeleting trainee...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        boolean success = traineeService.deleteTrainee(username, password);
        System.out.println(success ? "\nTrainee deleted successfully!" : "\nFailed to delete trainee.");
    }

    private static void createTrainer(TrainerService trainerService) {
        System.out.println("\nCreating new trainer...");
        String firstName = getInput("First Name: ");
        String lastName = getInput("Last Name: ");
        Long specializationId = Long.parseLong(getInput("Specialization ID: "));

        TrainerRequestDto request = new TrainerRequestDto(firstName, lastName, specializationId);
        TrainerResponseDto response = trainerService.createTrainer(request);

        System.out.println("\nTrainer created successfully!");
        printTrainer(response);
    }

    private static void getTrainer(TrainerService trainerService) {
        System.out.println("\nFetching trainer profile...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        try {
            TrainerResponseDto trainer = trainerService.getTrainerByUsername(username, password);
            printTrainer(trainer);
        } catch (Exception e) {
            System.out.println("\nError: " + e.getMessage());
        }
    }

    private static void printTrainer(TrainerResponseDto trainer) {
        System.out.println("\n----------------------------");
        System.out.println("Trainer Profile");
        System.out.println("----------------------------");
        System.out.println("First Name: " + trainer.getFirstName());
        System.out.println("Last Name: " + trainer.getLastName());
        System.out.println("Username: " + trainer.getUsername());
        System.out.println("Password: " + trainer.getPassword());
        System.out.println("Active: " + (trainer.getIsActive() ? "Yes" : "No"));
        System.out.println("Specialization ID: " + trainer.getSpecializationId());
        System.out.println("----------------------------");
    }

    private static void deleteTrainer(TrainerService trainerService) {
        System.out.println("\nDeleting trainer...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        boolean success = trainerService.deleteTrainer(username, password);
        System.out.println(success ? "\nTrainer deleted successfully!" : "\nFailed to delete trainer.");
    }

    private static void updateTrainer(TrainerService trainerService) {
        System.out.println("\nUpdating trainer profile...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        String firstName = getInput("New First Name (or press Enter to skip): ");
        String lastName = getInput("New Last Name (or press Enter to skip): ");
        String specializationIdStr = getInput("New Specialization ID (or press Enter to skip): ");
        String newPassword = getInput("New Password (or press Enter to skip): ");

        Long specializationId = specializationIdStr.isEmpty() ? null : Long.parseLong(specializationIdStr);

        TrainerRequestDto request = new TrainerRequestDto(
                firstName.isEmpty() ? null : firstName,
                lastName.isEmpty() ? null : lastName,
                specializationId
        );

        boolean updated = trainerService.updateTrainer(username, password, request, newPassword.isEmpty() ? null : newPassword);
        System.out.println(updated ? "\nTrainer profile updated successfully!" : "\nFailed to update trainer profile.");
    }

    private static void changeTrainerStatus(TrainerService trainerService) {
        System.out.println("\nToggling traine status...");
        String username = getInput("Username: ");
        String password = getInput("Password: ");

        boolean isActive = trainerService.changeTrainerStatus(username, password);
        System.out.println("\nTrainee status successfully changed! New status: " + (isActive ? "Active" : "Inactive"));
    }

    private static void changeTrainerPassword(TrainerService trainerService) {
        System.out.println("\nChanging trainer password...");
        String username = getInput("Username: ");
        String oldPassword = getInput("Old Password: ");
        String newPassword = getInput("New Password: ");

        boolean success = trainerService.changeTrainerPassword(username, oldPassword, newPassword);
        System.out.println(success ? "\nPassword changed successfully!" : "\nFailed to change password.");
    }

    private static void printTrainee(TraineeResponseDto trainee) {
        System.out.println("\n----------------------------");
        System.out.println("Trainee Profile");
        System.out.println("----------------------------");
        System.out.println("First Name: " + trainee.getFirstName());
        System.out.println("Last Name: " + trainee.getLastName());
        System.out.println("Username: " + trainee.getUsername());
        System.out.println("Password: " + trainee.getPassword());
        System.out.println("Date of Birth: " + new SimpleDateFormat("yyyy-MM-dd").format(trainee.getDateOfBirth()));
        System.out.println("Address: " + trainee.getAddress());
        System.out.println("Active: " + (trainee.isActive() ? "Yes" : "No"));
        System.out.println("----------------------------");
    }

    private static void addTraining(TrainingService trainingService) {
        System.out.println("\nAdding new training...");
        Long traineeId = Long.parseLong(getInput("Trainee ID: "));
        Long trainerId = Long.parseLong(getInput("Trainer ID: "));
        Long trainingTypeId = Long.parseLong(getInput("Training Type ID: "));
        String trainingName = getInput("Training Name: ");
        Date trainingDate = getDateInput("Training Date (yyyy-MM-dd): ");
        int trainingDuration = Integer.parseInt(getInput("Training Duration (minutes): "));

        TrainingRequestDto request = new TrainingRequestDto(traineeId, trainerId, trainingTypeId, trainingName, trainingDate, trainingDuration);
        TrainingResponseDto response = trainingService.addTraining(request);

        System.out.println("\nTraining added successfully: " + response.getTrainingName());
    }

    private static void getTrainerTrainings(TrainingService trainingService) {
        System.out.println("\nFetching trainer trainings...");
        String username = getInput("Trainer Username: ");
        String password = getInput("Password: ");
        Date fromDate = getDateInput("From Date (yyyy-MM-dd): ");
        Date toDate = getDateInput("To Date (yyyy-MM-dd): ");
        String traineeName = getInput("Trainee Name (or press Enter to skip): ");

        List<TrainingResponseDto> trainings = trainingService.getTrainerTrainings(username, password, fromDate, toDate, traineeName);

        if (trainings.isEmpty()) {
            System.out.println("No trainings found for this trainer.");
        } else {
            trainings.forEach(training -> System.out.println(
                    "\n--- Training Details ---" +
                            "\nTraining Name: " + training.getTrainingName() +
                            "\nTraining Type: " + training.getTrainingType() +
                            "\nTrainee: " + training.getTraineeName() +
                            "\nTrainer: " + training.getTrainerName() +
                            "\nDate: " + new SimpleDateFormat("yyyy-MM-dd").format(training.getTrainingDate()) +
                            "\nDuration: " + training.getTrainingDuration() + " minutes"
            ));
        }
    }

    private static void getTraineeTrainings(TrainingService trainingService) {
        System.out.println("\nFetching trainee trainings...");
        String username = getInput("Trainee Username: ");
        String password = getInput("Password: ");
        Date fromDate = getDateInput("From Date (yyyy-MM-dd): ");
        Date toDate = getDateInput("To Date (yyyy-MM-dd): ");
        String trainerName = getInput("Trainer Name (or press Enter to skip): ");
        String trainingType = getInput("Training Type (or press Enter to skip): ");

        List<TrainingResponseDto> trainings = trainingService.getTraineeTrainings(username, password, fromDate, toDate, trainerName, trainingType);

        if (trainings.isEmpty()) {
            System.out.println("No trainings found for this trainee.");
        } else {
            trainings.forEach(training -> System.out.println(
                    "\n--- Training Details ---" +
                            "\nTraining Name: " + training.getTrainingName() +
                            "\nTraining Type: " + training.getTrainingType() +
                            "\nTrainee: " + training.getTraineeName() +
                            "\nTrainer: " + training.getTrainerName() +
                            "\nDate: " + new SimpleDateFormat("yyyy-MM-dd").format(training.getTrainingDate()) +
                            "\nDuration: " + training.getTrainingDuration() + " minutes"
            ));
        }
    }

    private static void getUnassignedTrainers(TrainingService trainingService) {
        System.out.println("\nFetching unassigned trainers...");
        String username = getInput("Trainee Username: ");
        String password = getInput("Password: ");

        List<TrainerResponseDto> trainers = trainingService.getUnassignedTrainers(username, password);
        trainers.forEach(trainer -> System.out.println("\nTrainer: " + trainer.getFirstName() + " " + trainer.getLastName()));
    }

    private static void updateTraineeTrainers(TrainingService trainingService) {
        System.out.println("\nUpdating trainee's trainers...");
        String username = getInput("Trainee Username: ");
        String password = getInput("Password: ");
        System.out.println("Enter Trainer Usernames (comma-separated): ");
        List<String> trainerUsernames = List.of(scanner.nextLine().split(","));

        List<TrainerResponseDto> trainers = trainingService.updateTraineeTrainers(username, password, trainerUsernames);
        trainers.forEach(trainer -> System.out.println("\nTrainer Updated: " + trainer.getFirstName() + " " + trainer.getLastName()));
    }

    private static String getInput(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    private static Date getDateInput(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return new SimpleDateFormat("yyyy-MM-dd").parse(input);
            } catch (ParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }
}