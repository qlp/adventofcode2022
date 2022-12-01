package nl.q8p.aoc2022.day01;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import static java.lang.Math.max;
import static java.util.Arrays.stream;
import static java.util.Comparator.reverseOrder;

public class Day01 implements Day {

    @Override
    public Assignment first() {
        return input -> stream(input.split("\\n\\n")) // group for each Elve
                .map(group -> stream(group.split("\\n")) // list of calories (as String)
                .map(Integer::parseInt) // list of calories (as Int)
                .reduce(0, Integer::sum)) // sum of calories
                .reduce(0, (result, value) -> max(value, result)) // max of calories
                .toString();
    }

    @Override
    public Assignment second() {
        return input -> stream(input.split("\\n\\n")) // group for each Elve
                .map(group -> stream(group.split("\\n")) // list of calories (as String)
                .map(Integer::parseInt) // list of calories (as Int)
                .reduce(0, Integer::sum)) // sum of calories
                .sorted(reverseOrder()) // sorted descending
                .toList() // as a list
                .subList(0, 3)// first three elements
                .stream() // as a stream
                .reduce(0, Integer::sum) // sum of calories
                .toString();
    }
}
