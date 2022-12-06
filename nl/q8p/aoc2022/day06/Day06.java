package nl.q8p.aoc2022.day06;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day06 implements Day {

    @Override
    public Assignment first() {
        return input -> IntStream.range(4, input.length())
            .filter(i -> input.substring(i - 4, i).chars().boxed().collect(Collectors.toSet()).size() == 4)
            .findFirst()
            .orElseThrow();
    }

    @Override
    public Assignment second() {
        return input -> IntStream.range(14, input.length())
            .filter(i -> input.substring(i - 14, i).chars().boxed().collect(Collectors.toSet()).size() == 14)
            .findFirst()
            .orElseThrow();
    }
}
