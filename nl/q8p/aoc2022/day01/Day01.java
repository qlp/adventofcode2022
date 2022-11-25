package nl.q8p.aoc2022.day01;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

public class Day01 implements Day {

    @Override
    public Assignment first() {
        return input -> input.toUpperCase();
    }

    @Override
    public Assignment second() {
        return input -> input.toLowerCase();
    }
}
