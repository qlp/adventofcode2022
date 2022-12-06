package nl.q8p.aoc2022.day06;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.stream.Collectors;

public class Day06 implements Day {

    @Override
    public Assignment first() {
        return input -> {
            var i = 4;

            while (input.substring(i - 4, i).chars().boxed().collect(Collectors.toSet()).size() != 4) i++;

            return i;
        };
    }

    @Override
    public Assignment second() {
        return input -> {
            var i = 14;

            while (input.substring(i - 14, i).chars().boxed().collect(Collectors.toSet()).size() != 14) i++;

            return i;
        };
    }
}
