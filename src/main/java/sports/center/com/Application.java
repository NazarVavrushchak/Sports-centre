package sports.center.com;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sports.center.com.config.AppConfig;
import sports.center.com.enumeration.TrainingType;
import sports.center.com.facade.GymFacade;
import sports.center.com.model.Trainee;
import sports.center.com.model.Trainer;
import sports.center.com.model.Training;
import sports.center.com.util.PasswordUtil;
import sports.center.com.util.UsernameUtil;

import java.time.LocalDate;
import java.util.Scanner;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        logger.info("Starting Gym Management Application...");
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade gymFacade = context.getBean(GymFacade.class);
        logger.info("Application context initialized successfully.");
        runMenu(gymFacade);
    }

    private static void runMenu(GymFacade gymFacade) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            showMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createTrainee(scanner, gymFacade);
                    break;
                case "2":
                    createTrainer(scanner, gymFacade);
                    break;
                case "3":
                    createTraining(scanner, gymFacade);
                    break;
                case "4":
                    listAllTrainees(gymFacade);
                    break;
                case "5":
                    listAllTrainers(gymFacade);
                    break;
                case "6":
                    listAllTrainings(gymFacade);
                    break;
                case "7":
                    deleteTrainee(scanner, gymFacade);
                    break;
                case "8":
                    deleteTrainer(scanner, gymFacade);
                    break;
                case "9":
                    updateTrainee(scanner, gymFacade);
                    break;
                case "10":
                    updateTrainer(scanner, gymFacade);
                    break;
                case "11":
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\nGym Management System");
        System.out.println("1. Create Trainee");
        System.out.println("2. Create Trainer");
        System.out.println("3. Create Training");
        System.out.println("4. List All Trainees");
        System.out.println("5. List All Trainers");
        System.out.println("6. List All Trainings");
        System.out.println("7. Delete Trainee");
        System.out.println("8. Delete Trainer");
        System.out.println("9. Update Trainee");
        System.out.println("10. Update Trainer");
        System.out.println("11. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void createTrainee(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
        LocalDate dob = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        String username = UsernameUtil.generateUsername(firstName, lastName, gymFacade.getTraineeDao());
        String password = PasswordUtil.generatePassword();

        Trainee trainee = new Trainee(firstName, lastName, username, password, true, dob, address);
        gymFacade.createTrainee(trainee);
    }

    private static void createTrainer(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter Specialization: ");
        String specialization = scanner.nextLine();

        String username = UsernameUtil.generateUsername(firstName, lastName, gymFacade.getTrainerDao());
        String password = PasswordUtil.generatePassword();

        Trainer trainer = new Trainer(firstName, lastName, username, password, true, specialization);
        gymFacade.createTrainer(trainer);
    }

    private static void updateTrainee(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter Trainee ID to update: ");
        long id = Long.parseLong(scanner.nextLine());
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
        LocalDate dob = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();

        Trainee trainee = new Trainee(firstName, lastName, null, null, true, dob, address);
        gymFacade.updateTrainee(id, trainee);
    }

    private static void updateTrainer(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter Trainer ID to update: ");
        long id = Long.parseLong(scanner.nextLine());
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter Specialization: ");
        String specialization = scanner.nextLine();

        Trainer trainer = new Trainer(firstName, lastName, null, null, true, specialization);
        gymFacade.updateTrainer(id, trainer);
    }

    private static void createTraining(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter Trainee ID: ");
        long traineeId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter Trainer ID: ");
        long trainerId = Long.parseLong(scanner.nextLine());
        System.out.print("Enter Training Name: ");
        String trainingName = scanner.nextLine();
        System.out.print("Enter Training Type: ");
        String trainingType = scanner.nextLine();
        System.out.print("Enter Training Date (YYYY-MM-DD): ");
        LocalDate trainingDate = LocalDate.parse(scanner.nextLine());
        System.out.print("Enter Training Duration (minutes): ");
        int trainingDuration = Integer.parseInt(scanner.nextLine());

        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(trainingName);
        training.setTrainingType(TrainingType.valueOf(trainingType.toUpperCase()));
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);
        gymFacade.createTraining(training);
        System.out.println("Training created.");
    }

    private static void listAllTrainees(GymFacade gymFacade) {
        gymFacade.getAllTrainees().forEach(System.out::println);
    }

    private static void listAllTrainers(GymFacade gymFacade) {
        gymFacade.getAllTrainers().forEach(System.out::println);
    }

    private static void listAllTrainings(GymFacade gymFacade) {
        gymFacade.getAllTrainings().forEach(System.out::println);
    }

    private static void deleteTrainee(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter Trainee ID to delete: ");
        long id = Long.parseLong(scanner.nextLine());
        gymFacade.deleteTrainee(id);
    }

    private static void deleteTrainer(Scanner scanner, GymFacade gymFacade) {
        System.out.print("Enter Trainer ID to delete: ");
        long id = Long.parseLong(scanner.nextLine());
        gymFacade.deleteTrainer(id);
    }
}