package nl.q8p.aoc2022.day11;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.Comparator.reverseOrder;

public class Day11 implements Day {
    record Monkey(LinkedList<Long> items, Function<Long, Long> inspect, Long test, int ifTrue, int ifFalse, AtomicLong inspectedCount)
    implements Iterator<Long> {
        static Monkey parse(String string) {
            var parts = string.split("\\n");
            return new Monkey(
                new LinkedList<>(Arrays.stream(parts[1].split(": ")[1].split(", ")).map(Long::parseLong).toList()),
                monkeyFunction(parts[2].split(" = old ")[1]),
                Long.parseLong(parts[3].split(" by ")[1]),
                Integer.parseInt(parts[4].split(" monkey ")[1]),
                Integer.parseInt(parts[5].split(" monkey ")[1]),
                new AtomicLong()
            );
        }

        @Override
        public boolean hasNext() {
            return !items.isEmpty();
        }

        @Override
        public Long next() {
            inspectedCount.incrementAndGet();

            return inspect.apply(items.removeLast());
        }

        public int target(long bored) {
            return bored % test == 0L ? ifTrue : ifFalse;
        }

        public void add(long item) {
            items.add(item);
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> playGame(input, 20, 3);
    }

    @Override
    public Assignment second() {
        return (run, input) -> playGame(input, 10000, 1);
    }

    private static long playGame(String input, int numberOfRounds, long boredDivisor) {
        var monkeys = Arrays.stream(input.split("\\n\\n")).map(Monkey::parse).toList();

        IntStream.range(0, numberOfRounds).forEach(i -> round(monkeys, boredDivisor));

        return monkeys.stream()
                .mapToLong(m -> m.inspectedCount.get())
                .boxed()
                .sorted(reverseOrder())
                .mapToLong(i -> i)
                .limit(2)
                .reduce(1L, (a, b) -> a * b);
    }

    private static void round(List<Monkey> monkeys, Long boredDivisor) {
        var max = monkeys.stream().map(m -> m.test).reduce(1L, (a, b) -> a * b);

        monkeys.forEach(monkey -> monkey.forEachRemaining(inspected -> {
            var stress = (inspected / boredDivisor) % max;

            monkeys.get(monkey.target(stress)).add(stress);
        }));
    }

    public static Function<Long, Long> monkeyFunction(String function) {
        var parts = function.split(" ");
        var value = parts[1];

        return switch(parts[0]) {
            case "+" -> old -> old + Integer.parseInt(value);
            case "*" -> old -> old * (value.equals("old") ? old : Integer.parseInt(value));
            default -> throw new UnsupportedOperationException(parts[0]);
        };
    }
}
