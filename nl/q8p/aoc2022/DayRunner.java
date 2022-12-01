package nl.q8p.aoc2022;

import java.io.*;
import java.util.Objects;
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
        try {
            printAssignment(FIRST, day.first());
            printAssignment(SECOND, day.second());
        } catch (final Exception exception) {
            printException(exception);
        }
    }

    private void printHeader(final AssignmentType assignmentType) {
        printSeparator();
        log.info(() -> day.getClass().getSimpleName() + " - " + assignmentType);
        printSeparator();
    }

    private void printAssignment(final AssignmentType assignmentType, final Assignment assignment) {
        printHeader(assignmentType);
        try {
            final var assignmentData = readAssignmentData(assignmentType);

            run(assignment, assignmentData);


        } catch (final IOException e) {
            log.severe(() -> "Cannot read assignment data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void run(final Assignment assignment, final AssignmentData assignmentData) {
        try {
            final var actual = assignment.run(assignmentData.example);

            log.info(() -> "  EXAMPLE  : " + actual);
            if (!actual.equals(assignmentData.expected)) {
                log.info(() -> "  EXPECTING: " + assignmentData.expected);
            }
        } catch (final Exception exception) {
            log.info(() -> "  EXAMPLE  : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
        }

        try {
            final var actual = assignment.run(assignmentData.real);
            log.info(() -> "  REAL     : " + actual);
        } catch (final Exception exception) {
            log.info(() -> "  REAL     : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private record AssignmentData(String example, String expected, String real) {}

    private AssignmentData readAssignmentData(final AssignmentType assignmentType) throws IOException {
        return new AssignmentData(
            readFile("input-example.txt"),
            readFile("expected-" + assignmentType.name().toLowerCase() + ".txt"),
            readFile("input-real.txt")
        );
    }

    private String readFile(final String inputFileName) throws IOException {
        final var inputFileResourceName = inputFileResourceName(inputFileName);
        try (final var inputStream = day.getClass().getClassLoader().getResourceAsStream(inputFileResourceName)) {

            if (inputStream == null) {
                throw new IOException("File not found: " + inputFileResourceName);
            }

            try (Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
                final var writer = new StringWriter();

                final var buffer = new char[1024];

                var charsRead = reader.read(buffer);

                while (charsRead != -1) {
                    writer.write(buffer, 0, charsRead);

                    charsRead = reader.read(buffer);
                }

                return writer.toString();
            }
        }    }

    private String inputFileResourceName(final String inputFileName) {
        return day.getClass().getPackageName().replace('.', '/') + "/data/" + inputFileName;
    }

    private void printSeparator() {
        log.info(() -> "---------------------------------------------------------------------------");
    }

    private void printException(Exception exception) {
        printSeparator();
        log.severe(() -> day.getClass().getSimpleName() + ": cannot create assignment: " + exception);
        exception.printStackTrace();
        printSeparator();
    }
}
