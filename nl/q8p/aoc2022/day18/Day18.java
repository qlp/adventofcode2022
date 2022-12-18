package nl.q8p.aoc2022.day18;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Day18 implements Day {

    private static final Logger LOG = Logger.getLogger(Day18.class.getName());

    private static final List<List<Integer>> NEIGHBOURS = List.of(
            List.of(1, 0, 0),
            List.of(-1, 0, 0),
            List.of(0, 1, 0),
            List.of(0, -1, 0),
            List.of(0, 0, 1),
            List.of(0, 0, -1)
    );

    private static List<Integer> add(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < left.size(); i++) {
            result.add(left.get(i) + right.get(i));
        }

        return result;
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var world = Arrays.stream(input.split("\n")).map(l -> Arrays.stream(l.split(",")).mapToInt(Integer::parseInt).boxed().toList()).collect(Collectors.toSet());

            return world.stream().mapToLong(i -> NEIGHBOURS.stream().filter(n -> !world.contains(add(i, n))).count()).sum();
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
