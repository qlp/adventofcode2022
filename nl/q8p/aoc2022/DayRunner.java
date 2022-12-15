package nl.q8p.aoc2022;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Logger;

public record DayRunner(Day day, AssignmentType assignmentType) {
    private static final Logger log = Logger.getLogger(DayRunner.class.getName());

    private static final int WIDTH = 70;

    enum AssignmentType {
        FIRST, SECOND
    }

    void run() {
        try {
            printAssignment(
                switch (assignmentType) {
                    case FIRST -> day.first();
                    case SECOND -> day.second();
                }
            );
        } catch (final Exception exception) {
            printException(exception);
        }
    }

    private void printHeader(final AssignmentType assignmentType) {
        printSeparator();
        log.info(() -> day.getClass().getSimpleName() + " - " + assignmentType);
        printSeparator();
    }

    private void printAssignment(final Assignment assignment) {
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
            final var actual = run(() -> assignment.run(Assignment.Run.EXAMPLE, assignmentData.example).toString());
            logResult(actual, "EXAMPLE");
            if (!actual.result.equals(assignmentData.expected)) {
                log.info(() -> "  EXPECTING : " + assignmentData.expected);
            }
        } catch (final Exception exception) {
            log.info(() -> "  EXAMPLE  : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
        }

        try {
            final var actual = run(() -> assignment.run(Assignment.Run.ACTUAL, assignmentData.real).toString());
            logResult(actual, "REAL");
        } catch (final Exception exception) {
            log.info(() -> "  REAL     : EXCEPTION: " + exception.getMessage());
            exception.printStackTrace();
        }
    }

    private void logResult(Duration<String> duration, String phase) {
        var prefex = "  " + phase + " ".repeat(10 - phase.length()) + ": ";
        var suffix = duration.nanosAsMs();

        var shouldLogSeparateLines = duration.result.contains("\n") || duration.result.length() > (WIDTH - prefex.length() - suffix.length() - 5);

        if (shouldLogSeparateLines) {
            var outputWidth = Arrays.stream(duration.result.split("\n")).mapToInt(String::length).max().orElseThrow();
            var outputSeparator = "=".repeat(outputWidth);

            log.info(() -> rightAlign(prefex, suffix) + "\n" + outputSeparator + "\n" + duration.result + "\n" + outputSeparator);
        } else {
            log.info(() -> rightAlign(prefex + duration.result, suffix));
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
        log.info(() -> "-".repeat(WIDTH));
    }

    private String rightAlign(String line, String rightAligned) {
        return line + " ".repeat(Math.max(1, WIDTH - line.length() - rightAligned.length())) + rightAligned;
    }

    private void printException(Exception exception) {
        printSeparator();
        log.severe(() -> day.getClass().getSimpleName() + ": cannot create assignment: " + exception);
        exception.printStackTrace();
        printSeparator();
    }

    record Duration<T>(T result, long nanos) {
        public String nanosAsMs() {
            return new DecimalFormat("#0.00 ms", new DecimalFormatSymbols(Locale.US)).format(((double)nanos) / 1_000_000);
        }

        @Override
        public String toString() {
            return result.toString();
        }
    }

    private <T> Duration<T> run(Supplier<T> function) {
        long start = System.nanoTime();
        T result = function.get();
        long nanos = System.nanoTime() - start;

        return new Duration<>(result, nanos);
    }
}
