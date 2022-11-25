package nl.q8p.aoc2022;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static nl.q8p.aoc2022.DayRunner.AssignmentType.FIRST;
import static nl.q8p.aoc2022.DayRunner.AssignmentType.SECOND;

public class DayRunner {
    private final Day day;

    private final Logger log = Logger.getLogger(DayRunner.class.getName());

    enum AssignmentType {
        FIRST, SECOND
    }

    public DayRunner(Day day) {
        this.day = day;
    }

    void run() {
        printAssignment(FIRST, day.first());
        printAssignment(SECOND, day.second());
    }

    private void printHeader(final AssignmentType assignmentType) {
        printSeparator();
        log.info(() -> day.getClass().getSimpleName() + " - " + assignmentType);
        printSeparator();
    }

    private void printAssignment(AssignmentType assignmentType, Assignment assignment) {
        printHeader(assignmentType);
        try {
            final AssignmentData assignmentData = readAssignmentData(assignmentType);

            run(assignment, assignmentData);


        } catch (IOException e) {
            log.severe(() -> "Cannot read assignment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void run(Assignment assignment, AssignmentData assignmentData) {
        try {
            final String actual = assignment.run(assignmentData.example);

            log.info(() -> "  EXAMPLE  : " + actual);
            if (!actual.equals(assignmentData.expected)) {
                log.info(() -> "  EXPECTING: " + assignmentData.expected);
            }
        } catch (Exception exception) {
            log.info(() -> "  EXAMPLE  : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
            printSeparator();
        }

        try {
            final String actual = assignment.run(assignmentData.real);
            log.info(() -> "  REAL     : " + actual);
        } catch (Exception exception) {
            log.info(() -> "  REAL     : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
            printSeparator();
        }
    }

    private record AssignmentData(String example, String expected, String real) {}

    private AssignmentData readAssignmentData(AssignmentType assignmentType) throws IOException {
        return new AssignmentData(
            readFile(assignmentType, "example.txt"),
            readFile(assignmentType, "expected.txt"),
            readFile(assignmentType, "real.txt")
        );
    }

    private String readFile(AssignmentType assignmentType, String inputFileName) throws IOException {
        final String inputFileResourceName = inputFileResourceName(assignmentType, inputFileName);
        final URL resource = day.getClass().getClassLoader().getResource(inputFileResourceName);

        if (resource == null) {
            throw new IOException("File not found: " + inputFileResourceName);
        }

        return Files.readString(Path.of(resource.getPath()));
    }

    private String inputFileResourceName(AssignmentType assignmentType, String inputFileName) {
        return day.getClass().getPackageName().replace('.', '/') + "/data/" + assignmentType.name().toLowerCase() + "/" + inputFileName;
    }

    private void printSeparator() {
        log.info(() -> "-------------------------");
    }
}
