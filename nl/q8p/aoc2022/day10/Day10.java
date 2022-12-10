package nl.q8p.aoc2022.day10;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;

public class Day10 implements Day {

    @Override
    public Assignment first() {
        return input -> {
            var cpu = cpu(input);

            var total = 0;

            for (int i = 20; i <= 220; i += 40) {
                total += i * cpu.get(i - 1);
            }

            return total;
        };
    }

    @Override
    public Assignment second() {
        return input -> {
            var cpu = cpu(input);

            var output = new StringBuilder();

            for (var cycle = 1; cycle <= 240; cycle++) {
                var position = (cycle - 1) % 40;

                if (cycle != 1 && position == 0) {
                    output.append('\n');
                }

                var x = cpu.get(cycle - 1);

                if (position >= x - 1 && position <= x + 1) {
                    output.append('#');
                } else {
                    output.append('.');
                }

            }

            return output.toString();
        };
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
