package nl.q8p.aoc2022;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
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
            String actual = run(assignment, assignmentType, "example.txt");

            String expected = readFile(assignmentType, "expected.txt");
            log.info(() -> "  EXAMPLE  : " + actual);
            if (!actual.equals(expected)) {
                log.info(() -> "  EXPECTING: " + expected);
            }
        } catch (Exception exception) {
            log.info(() -> "  EXAMPLE  : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
            printSeparator();
        }

        try {
            String actual = run(assignment, assignmentType, "real.txt");
            log.info(() -> "  REAL     : " + actual);
        } catch (Exception exception) {
            log.info(() -> "  REAL     : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
            printSeparator();
        }
    }

    private String run(Assignment assignment, AssignmentType assignmentType, String inputFileName) throws Exception {
        return assignment.run(readFile(assignmentType, inputFileName));
    }

    private String readFile(AssignmentType assignmentType, String inputFileName) throws IOException {
        final String inputFileResourceName = inputFileResourceName(assignmentType, inputFileName);
        try (InputStream inputStream = day.getClass().getClassLoader().getResourceAsStream(inputFileResourceName)) {

            if (inputStream == null) {
                throw new IOException("File not found: " + inputFileResourceName);
            }

            try (Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
                StringWriter writer = new StringWriter();

                char[] buffer = new char[1024];

                int charsRead = reader.read(buffer);

                while (charsRead != -1) {
                    writer.write(buffer, 0, charsRead);

                    charsRead = reader.read(buffer);
                }

                return writer.toString();
            }
        }
    }

    private String inputFileResourceName(AssignmentType assignmentType, String inputFileName) {
        return day.getClass().getPackageName().replace('.', '/') + "/data/" + assignmentType.name().toLowerCase() + "/" + inputFileName;
    }

    private void printSeparator() {
        log.info(() -> "-------------------------");
    }
}
