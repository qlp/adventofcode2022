package nl.q8p.aoc2022.day05;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;

import static java.util.Arrays.stream;

public class Day05 implements Day {

    record Move(int count, int from, int to) {
        static Move parse(String string) {
            var parts = string.split(" ");

            return new Move(Integer.parseInt(parts[1]), Integer.parseInt(parts[3]), Integer.parseInt(parts[5]));
        }
    }

    static class Supplies9000 {
        private final List<LinkedList<Character>> stacks = new ArrayList<>();

        void handle(String instruction) {
            if (instruction.indexOf('[') != -1) {
                // building stacks
                for (var i = 1; i < instruction.length(); i += 4) {
                    var crate = instruction.charAt(i);

                    if (crate != ' ') {
                        var stackIndex = (i - 1) / 4;

                        while (stacks.size() <= stackIndex) {
                            stacks.add(new LinkedList<>());
                        }

                        stacks.get(stackIndex).add(0, crate);
                    }
                }
            } else if (instruction.startsWith("move")) {
                var move = Move.parse(instruction);

                for (var i = 0; i < move.count; i++) {
                    stacks.get(move.to - 1).add(stacks.get(move.from - 1).removeLast());
                }
            }
        }
    }

    static class Supplies9001 {
        private final List<List<Character>> stacks = new ArrayList<>();

        void handle(String instruction) {
            if (instruction.indexOf('[') != -1) {
                // building stacks
                for (var i = 1; i < instruction.length(); i += 4) {
                    var crate = instruction.charAt(i);

                    if (crate != ' ') {
                        var stackIndex = (i - 1) / 4;

                        while (stacks.size() <= stackIndex) {
                            stacks.add(new LinkedList<>());
                        }

                        stacks.get(stackIndex).add(0, crate);
                    }
                }
            } else if (instruction.startsWith("move")) {
                var move = Move.parse(instruction);

                var from = stacks.get(move.from - 1);

                stacks.get(move.to - 1).addAll(from.subList(from.size() - move.count, from.size()));
                stacks.set(move.from - 1, from.subList(0, from.size() - move.count));
            }
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var supplies = new Supplies9000();

            stream(input.split("\\n")).forEach(supplies::handle);


            return supplies.stacks.stream().map(LinkedList::peekLast).collect(Collector.of(
                    StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append,
                    StringBuilder::toString));
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var supplies = new Supplies9001();

            stream(input.split("\\n")).forEach(supplies::handle);

            return supplies.stacks.stream().map(s -> s.get(s.size() - 1)).collect(Collector.of(
                    StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append,
                    StringBuilder::toString));
        };
    }
}
