package nl.q8p.aoc2022.day01;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Collections;

import static java.util.Arrays.stream;

public class Day01 implements Day {

    @Override
    public Assignment first() {
        return (run, input) -> stream(input.split("\\n\\n")) // group for each Elve
            .mapToInt(group -> stream(group.split("\\n")) // list of calories (as String)
                .mapToInt(Integer::parseInt) // list of calories (as Int)
                .sum() // sum of calories of elve
            ) // total of calories for each elve
            .max() // get the max
            .orElse(0);
    }

    @Override
    public Assignment second() {
        return (run, input) -> 
            stream(input.split("\\n\\n")) // group for each Elve
            .map(group -> stream(group.split("\\n")) // list of calories (as String)
                .mapToInt(Integer::parseInt) // list of calories (as Int)
                .sum() // sum of calories
            )
            .sorted(Collections.reverseOrder()) // sort descending
            .mapToInt(Integer::intValue)
            .limit(3) // first three
            .sum(); // sum
    }
}
