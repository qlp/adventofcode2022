package nl.q8p.aoc2022.day25;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.logging.Logger;

public class Day25 implements Day {

    private static final Logger LOG = Logger.getLogger(Day25.class.getName());

    static BigInteger parse(String string) {
        var result = BigInteger.ZERO;
        for (int i = 0; i < string.length(); i++) {
            var multiplier = BigInteger.valueOf(5).pow(i);
            var symbol = string.charAt(string.length() - 1 - i);
            var number = switch (symbol) {
                case '=' -> -2;
                case '-' -> -1;
                case '0' -> 0;
                case '1' -> 1;
                case '2' -> 2;
                default -> throw new IllegalStateException("Invalid input: '" + string +"' at " + (string.length() - 1 - i));
            };
            var value = multiplier.multiply(BigInteger.valueOf(number));

            result = result.add(value);
        }

        return result;
    }

    static String format(BigInteger value) {
        var result = new StringBuilder(value.toString(5));

        var pos = 0;

        while (pos < result.length()) {
            var symbol = result.charAt(result.length() - 1 - pos);

            switch (symbol) {
                case '3' -> fix(result, pos, '=');
                case '4' -> fix(result, pos, '-');
            }

            pos++;
        }

        return result.toString();
    }

    static void fix(StringBuilder value, int position, char replacement) {
        value.setCharAt(value.length() - 1 - position, replacement);

        fix5(value, position);
    }

    static void fix5(StringBuilder value, int position) {
        if (position + 2 == value.length() && value.charAt(value.length() - 2 - position) == '4') {
            value.insert(0, '1');
        } else {
            var symbolToFix = value.charAt(value.length() - 2 - position);

            if (symbolToFix == '4') {
                fix5(value, position + 1);
                value.setCharAt(value.length() - 2 - position, '0');
            } else {
                var newSymbol = switch (symbolToFix) {
                    case '0' -> '1';
                    case '1' -> '2';
                    case '2' -> '3';
                    case '3' -> '4';
                    default -> throw new IllegalStateException("Unexpected decimal symbolTo '" + symbolToFix + "' in '" + value + "' at " + position);
                };

                value.setCharAt(value.length() - 2 - position, newSymbol);
            }
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var numbers = Arrays.stream(input.split("\\n")).map(Day25::parse).toList();

            var total = BigInteger.ZERO;

            for (var number : numbers) {
                total = total.add(number);
            }

            return format(total);
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
