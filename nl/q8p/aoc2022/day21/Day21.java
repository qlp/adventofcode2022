package nl.q8p.aoc2022.day21;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.math.MathContext.DECIMAL128;

public class Day21 implements Day {

    private static MathContext mathContext = DECIMAL128;

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
                    ));
        }
    }

    sealed interface Monkey permits NumberMonkey, AddMonkey, SubtractMonkey, MultiplyMonkey, DivideMonkey, RootMonkey, Me {
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
            } else if (string.contains("=")) {
                return new RootMonkey(
                    string.substring(0, string.indexOf(' ')),
                    string.substring(string.lastIndexOf(' ') + 1));
            } else if (string.equals("me")) {
                return new Me();
            } else {
                return new NumberMonkey(new BigDecimal(string));
            }
        }

        BigDecimal getAnswer(World world);

        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world);


        default BigDecimal getAnswerReturningNullWhenDependingOnMe(World world) {
            try {
                return getAnswer(world);
            } catch (MeException e) {
                return null;
            }
        }
    }

    record Me() implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            throw new MeException();
        }

        @Override
        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            throw new UnsupportedOperationException("Cannot invert me");
        }

        @Override
        public String toString() {
            return "me";
        }
    }

    static class MeException extends RuntimeException {
        MeException() {
            super("I don't know the answer");
        }
    };

    record RootMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            var leftMonkey = world.get(left);
            var rightMonkey = world.get(right);

            return solve(leftMonkey, rightMonkey, world);
        }

        private BigDecimal solve(Monkey left, Monkey right, World world) {
            var leftAnswer = left.getAnswerReturningNullWhenDependingOnMe(world);
            var rightAnswer = right.getAnswerReturningNullWhenDependingOnMe(world);

            var dependingOnMe = leftAnswer == null ? left : right;

            var answer = leftAnswer != null ? leftAnswer : rightAnswer;

            if (dependingOnMe instanceof Me) {
                return answer;
            } else if (dependingOnMe instanceof NumberMonkey m) {
                throw new IllegalStateException("Not expecting me");
            } else {
                var monkeyWithCorrectedNumber = dependingOnMe.invert(answer, world);

                return solve(monkeyWithCorrectedNumber.monkey, new NumberMonkey(monkeyWithCorrectedNumber.number), world);
            }
        }

        @Override
        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            throw new UnsupportedOperationException("Cannot invert root");
        }

        @Override
        public String toString() {
            return left + " = " + right;
        }
    }

    record NumberMonkey(BigDecimal number) implements Monkey {

        @Override
        public BigDecimal getAnswer(World world) {
            return number;
        }

        @Override
        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            throw new UnsupportedOperationException("Cannot invert number");
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

        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            var leftMonkey = world.get(left);
            var leftAnswer = leftMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            var rightMonkey = world.get(right);
            var rightAnswer = rightMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            if (leftAnswer == null) {
                return new MonkeyWithCorrectedNumber(leftMonkey, value.subtract(rightAnswer));
            } else if (rightAnswer == null) {
                return new MonkeyWithCorrectedNumber(rightMonkey, value.subtract(leftAnswer));
            } else {
                return new MonkeyWithCorrectedNumber(new NumberMonkey(leftAnswer.add(rightAnswer)), value);
            }
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

        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            var leftMonkey = world.get(left);
            var leftAnswer = leftMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            var rightMonkey = world.get(right);
            var rightAnswer = rightMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            if (leftAnswer == null) {
                return new MonkeyWithCorrectedNumber(leftMonkey, value.add(rightAnswer));
            } else if (rightAnswer == null) {
                return new MonkeyWithCorrectedNumber(rightMonkey, leftAnswer.subtract(value));
            } else {
                return new MonkeyWithCorrectedNumber(new NumberMonkey(leftAnswer.subtract(rightAnswer)), value);
            }
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

        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            var leftMonkey = world.get(left);
            var leftAnswer = leftMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            var rightMonkey = world.get(right);
            var rightAnswer = rightMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            if (leftAnswer == null) {
                return new MonkeyWithCorrectedNumber(leftMonkey, value.divide(rightAnswer, mathContext));
            } else if (rightAnswer == null) {
                return new MonkeyWithCorrectedNumber(rightMonkey, value.divide(leftAnswer, mathContext));
            } else {
                return new MonkeyWithCorrectedNumber(new NumberMonkey(leftAnswer.multiply(rightAnswer)), value);
            }
        }
    }

    record MonkeyWithCorrectedNumber(Monkey monkey, BigDecimal number) { }

    record DivideMonkey(String left, String right) implements Monkey {
        @Override
        public BigDecimal getAnswer(World world) {
            return world.get(left).getAnswer(world).divide(world.get(right).getAnswer(world));
        }

        public MonkeyWithCorrectedNumber invert(BigDecimal value, World world) {
            var leftMonkey = world.get(left);
            var leftAnswer = leftMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            var rightMonkey = world.get(right);
            var rightAnswer = rightMonkey.getAnswerReturningNullWhenDependingOnMe(world);

            if (leftAnswer == null) {
                return new MonkeyWithCorrectedNumber(leftMonkey, value.multiply(rightAnswer, mathContext));
            } else if (rightAnswer == null) {
                return new MonkeyWithCorrectedNumber(rightMonkey, value.divide(leftAnswer, mathContext));
            } else {
                return new MonkeyWithCorrectedNumber(new NumberMonkey(leftAnswer.divide(rightAnswer, mathContext)), value);
            }
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

            return world.get("root").getAnswer(world);
        };
    }

    enum Result {
        LOWER,
        EQUAL,
        HIGHER,
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var world = World.parse(fix(input));

            return world.get("root").getAnswer(world);
        };
    }

    private static String fix(String input) {
        return Arrays.stream(input.split("\\n")).map(line -> {
            if (line.startsWith("root")) {
                return line.replace(line.charAt(line.lastIndexOf(' ') - 1), '=');
            } else if (line.startsWith("humn")) {
                return "humn: me";
            } else {
                return line;
            }
        }).collect(Collectors.joining("\n"));
    }
}
