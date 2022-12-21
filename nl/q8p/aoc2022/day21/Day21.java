package nl.q8p.aoc2022.day21;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Day21 implements Day {

    private static final Logger LOG = Logger.getLogger(Day21.class.getName());

    record World(Map<String, Monkey> monkeys) {
        Monkey get(String name) {
            return monkeys.get(name);
        }

        @Override
        public String toString() {
            return "monkeys\n" +
                String.join("\n", monkeys.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).toList());
        }

        static World parse(String input) {
            return new World(Arrays.stream(input.split("\\n"))
                    .collect(Collectors.toMap(
                            line -> line.substring(0, line.indexOf(':')),
                            line -> Monkey.parse(line.substring(line.indexOf(':') + 2)))
                    )
            );
        }
    }

    sealed interface Monkey permits NumberMonkey, AddMonkey, SubtractMonkey, MultiplyMonkey, DivideMonkey {
        static Monkey parse(String string) {
            if (string.contains("+")) {
                return new AddMonkey(
                    string.substring(0, string.indexOf(' ')),
                    string.substring(string.lastIndexOf(' ') + 1));
            } else if (string.contains("-")) {
                return new SubtractMonkey(
                    string.substring(0, string.indexOf(' ')),
                    string.substring(string.lastIndexOf(' ') + 1));
            } else if (string.contains("*")) {
                return new MultiplyMonkey(
                    string.substring(0, string.indexOf(' ')),
                    string.substring(string.lastIndexOf(' ') + 1));
            } else if (string.contains("/")) {
                return new DivideMonkey(
                    string.substring(0, string.indexOf(' ')),
                    string.substring(string.lastIndexOf(' ') + 1));
            } else {
                return new NumberMonkey(new BigDecimal(string));
            }
        }

        BigDecimal getAnswer(World world);
    }

    record NumberMonkey(BigDecimal number) implements Monkey {

        @Override
        public BigDecimal getAnswer(World world) {
            return number;
        }

        @Override
        public String toString() {
            return number.toString();
        }
    }

    record AddMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            return world.get(left).getAnswer(world).add(world.get(right).getAnswer(world));
        }

        @Override
        public String toString() {
            return left + " + " + right;
        }
    }

    record SubtractMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            return world.get(left).getAnswer(world).subtract(world.get(right).getAnswer(world));
        }

        @Override
        public String toString() {
            return left + " - " + right;
        }
    }

    record MultiplyMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            return world.get(left).getAnswer(world).multiply(world.get(right).getAnswer(world));
        }

        @Override
        public String toString() {
            return left + " * " + right;
        }
    }

    record DivideMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            return world.get(left).getAnswer(world).divide(world.get(right).getAnswer(world));
        }

        @Override
        public String toString() {
            return left + " - " + right;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var world = World.parse(input);

            LOG.info(world.toString());

            return world.get("root").getAnswer(world);
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
