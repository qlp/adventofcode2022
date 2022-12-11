package nl.q8p.aoc2022.day10;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public class Day10 implements Day {

    @Override
    public Assignment first() {
        return input -> IntStream.range(0, 6).map(i -> (i * 40 + 20) * cpu(input).get((i * 40 + 20) - 1)).sum();
    }

    @Override
    public Assignment second() {
        return input -> String.join("\n", IntStream.rangeClosed(1, 240).mapToObj(cycle -> {
            var position = (cycle - 1) % 40;

            var x = cpu(input).get(cycle - 1);

            return IntStream.rangeClosed(x - 1, x + 1).anyMatch(i -> i == position) ? '#' : '.';
        }).collect(Collector.of(
                StringBuilder::new,
                StringBuilder::append,
                StringBuilder::append,
                StringBuilder::toString)).split("(?<=\\G.{" + 40 + "})"));
    }

    private static ArrayList<Integer> cpu(String input) {
        var x = 1;

        var output = new ArrayList<Integer>();

        for(var instruction : Arrays.stream(input.split("\\n")).toList()) {
            if ("noop".equals(instruction)) {
                output.add(x);
            } else {
                output.add(x);
                output.add(x);
                x += Integer.parseInt(instruction.split(" ")[1]);
            }
        }
        output.add(x);
        return output;
    }
}
