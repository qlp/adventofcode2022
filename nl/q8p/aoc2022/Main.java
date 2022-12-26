package nl.q8p.aoc2022;

import nl.q8p.aoc2022.Assignment.Run;
import nl.q8p.aoc2022.day01.Day01;
import nl.q8p.aoc2022.day02.Day02;
import nl.q8p.aoc2022.day03.Day03;
import nl.q8p.aoc2022.day04.Day04;
import nl.q8p.aoc2022.day05.Day05;
import nl.q8p.aoc2022.day06.Day06;
import nl.q8p.aoc2022.day07.Day07;
import nl.q8p.aoc2022.day08.Day08;
import nl.q8p.aoc2022.day09.Day09;
import nl.q8p.aoc2022.day10.Day10;
import nl.q8p.aoc2022.day11.Day11;
import nl.q8p.aoc2022.day12.Day12;
import nl.q8p.aoc2022.day13.Day13;
import nl.q8p.aoc2022.day14.Day14;
import nl.q8p.aoc2022.day15.Day15;
import nl.q8p.aoc2022.day16.Day16;
import nl.q8p.aoc2022.day17.Day17;
import nl.q8p.aoc2022.day18.Day18;
import nl.q8p.aoc2022.day19.Day19;
import nl.q8p.aoc2022.day20.Day20;
import nl.q8p.aoc2022.day21.Day21;
import nl.q8p.aoc2022.day22.Day22;
import nl.q8p.aoc2022.day23.Day23;
import nl.q8p.aoc2022.day24.Day24;
import nl.q8p.aoc2022.day25.Day25;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.err;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private final String assignmentFilter;
    private final String runFilter;
    private final int preHeatSeconds;

    public Main(String assignmentFilter, String runFilter, int preHeatSeconds) {
        this.assignmentFilter = assignmentFilter;
        this.runFilter = runFilter;
        this.preHeatSeconds = preHeatSeconds;
    }

    public static void main(String[] args) {
        configureLogging();

        new Main(args.length < 1 ? null : args[0], args.length < 2 ? null : args[1], args.length < 3 ? -1 : Integer.parseInt(args[2])).run();
    }

    private static void configureLogging() {
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            err.println("Problem configuring logging" + e);
            e.printStackTrace();
        }
    }

    public void run() {
        loadDays()
                .stream().flatMap(day -> Arrays.stream(DayRunner.AssignmentType.values()).map(assignmentType -> new DayRunner(day, assignmentType)))
                .filter((dayRunner -> assignmentFilter == null || (dayRunner.day().getClass().getSimpleName() + "#" + dayRunner.assignmentType().name()).matches(assignmentFilter)))
                .forEach(dayRunner -> dayRunner.run(runs(), preHeatSeconds));
    }

    private List<Run> runs() {
        return Arrays
            .stream(Run.values())
            .filter(run -> runFilter == null || run.name().matches(runFilter))
            .toList();
    }

    private static List<Day> loadDays() {
        final var days = new ArrayList<Day>();
        days.add(new Day01());
        days.add(new Day02());
        days.add(new Day03());
        days.add(new Day04());
        days.add(new Day05());
        days.add(new Day06());
        days.add(new Day07());
        days.add(new Day08());
        days.add(new Day09());
        days.add(new Day10());
        days.add(new Day11());
        days.add(new Day12());
        days.add(new Day13());
        days.add(new Day14());
        days.add(new Day15());
        days.add(new Day16());
        days.add(new Day17());
        days.add(new Day18());
        days.add(new Day19());
        days.add(new Day20());
        days.add(new Day21());
        days.add(new Day22());
        days.add(new Day23());
        days.add(new Day24());
        days.add(new Day25());

        return days;
    }
}
