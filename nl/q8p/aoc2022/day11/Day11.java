package nl.q8p.aoc2022.day11;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day11 implements Day {
    private static final Logger LOG = Logger.getLogger(Day11.class.getName());

    record Monkey(LinkedList<BigInteger> items, Function<BigInteger, BigInteger> inspect, BigInteger test, int ifTrue, int ifFalse, AtomicLong inspectedCount) {
    }

    @Override
    public Assignment first() {
        return input -> playGame(input, 20, 3);
    }

    @Override
    public Assignment second() {
        return input -> playGame(input, 10000, 1);
    }


    private static long playGame(String input, int numberOfRounds, int boredDevisor) {
        var monkeys = parse(input);

        IntStream.range(0, numberOfRounds).forEach(i -> {
            playRound(monkeys, BigInteger.valueOf(boredDevisor));

            LOG.info("round " + i);
        });

        return monkeys.stream().mapToLong(m -> m.inspectedCount.get()).boxed().sorted(Comparator.reverseOrder()).mapToLong(i -> i).limit(2).reduce(1L, (a, b) -> a * b);
    }

    private static List<Monkey> parse(String input) {
        return Arrays.stream(input
                        .split("\\n\\n"))
                .map(monkey -> new Monkey(
                                new LinkedList<>(Arrays.stream(monkey.split("\\n")[1].split(": ")[1].split(", ")).map(BigInteger::new).toList()),
                                monkeyFunction(monkey.split("\\n")[2].split(" = old ")[1]),
                                new BigInteger(monkey.split("\\n")[3].split(" by ")[1]),
                                Integer.parseInt(monkey.split("\\n")[4].split(" monkey ")[1]),
                                Integer.parseInt(monkey.split("\\n")[5].split(" monkey ")[1]),
                                new AtomicLong()
                        )
                ).toList();
    }

    private static void playRound(List<Monkey> monkeys, BigInteger boredDevisor) {
        monkeys.forEach(monkey -> {
            while(!monkey.items.isEmpty()) {
                var next = monkey.items.removeFirst();

                monkey.inspectedCount.incrementAndGet();

                var inspected = monkey.inspect().apply(next);
                var bored = inspected;

                if (!boredDevisor.equals(BigInteger.ONE)) {
                    bored = inspected.divide(boredDevisor);
                }

                var target = bored.mod(monkey.test).equals(BigInteger.ZERO) ? monkey.ifTrue : monkey.ifFalse;

                monkeys.get(target).items.add(bored);
            }
        });
    }

    public static Function<BigInteger, BigInteger> monkeyFunction(String function) {
        if (function.equals("* old")) {
            return old -> old.pow(2);
        } else {
            var parts = function.split(" ");
            var opration = parts[0];
            var value = new BigInteger(parts[1]);

            return switch(opration) {
                case "+" -> old -> old.add(value);
                case "*" -> old -> old.multiply(value);
                default -> throw new UnsupportedOperationException(function);
            };
        }
    }
}
