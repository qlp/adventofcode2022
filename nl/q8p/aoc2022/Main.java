package nl.q8p.aoc2022;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.*;

public class Main {

    static final Logger log = Logger.getLogger(Main.class.getName());

    final String filter;

    public Main(String filter) {
        this.filter = filter;
    }

    public static void main(String[] args) {
        configureLogging();

        new Main(args.length == 0 ? null : args[0]).run();
    }

    private static void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(Files.newInputStream(Path.of("logging.properties")));
        } catch (IOException e) {
            err.println("Problem configuring logging" + e);
            e.printStackTrace();
        }
    }

    public void run() {
        loadDays()
                .stream().filter((day -> filter == null || day.getClass().getSimpleName().matches(filter)))
                .forEach((day -> new DayRunner(day).run()));
    }

    private static List<Day> loadDays() {
        List<Day> days = new ArrayList<>();

        for (int i = 1; i <= 25; i++) {
            String dayNumberString = String.format("%02d", i);

            String className = Day.class.getPackageName() + ".day" + dayNumberString + ".Day" + dayNumberString;
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> constructor = clazz.getConstructor();
                Object candidate = constructor.newInstance();

                if (candidate instanceof Day day) {
                    days.add(day);
                    log.info(() -> "Day" + dayNumberString + " is found");
                } else {
                    log.warning(() -> "Day" + dayNumberString + " is not a Day");
                }
            } catch (ClassNotFoundException e) {
                // No class for this day yet.
                log.fine(() -> "No class '" + className + "' found.");
            } catch (ReflectiveOperationException e) {
                log.severe(() -> "Day" + dayNumberString + " cannot be created: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return days;
    }
}
