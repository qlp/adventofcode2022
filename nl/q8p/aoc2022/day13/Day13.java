package nl.q8p.aoc2022.day13;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day13 implements Day {

    sealed interface Value extends Comparable<Value> permits Single, Multiple { }

    record Single(int value) implements Value {
        public static int parse(String string, int position, List<Value> values) {
            var buffer = new StringBuilder();
            while(string.charAt(position) >= '0' && string.charAt(position) <= '9') {
                buffer.append(string.charAt(position));
                position++;
            }

            values.add(new Single(Integer.parseInt(buffer.toString())));

            return position;
        }

        @Override
        public int compareTo(Value v) {
            return compareValues(this, v);
        }
    }

    static int compareValues(Value left, Value right) {
        if ((left instanceof Single l) && (right instanceof Single r)) {
            return Integer.compare(l.value, r.value);
        } else if ((left instanceof Multiple l) && (right instanceof Multiple r)) {
            int position = 0;
            while (position < l.values.size() && position < r.values.size()) {
                int result = compareValues(l.values.get(position), r.values.get(position));

                if (result != 0) {
                    return result;
                }

                position++;
            }

            return Integer.compare(l.values.size(), r.values.size());
        } else if ((left instanceof Single s)) {
            return compareValues(new Multiple(Collections.singletonList(s)), right);
        } else {
            return compareValues(left, new Multiple(Collections.singletonList(right)));
        }
    }

    record Multiple(List<Value> values) implements Value {
        static Multiple parse(String string) {
            var values = new ArrayList<Value>();

            var position = Multiple.parse(string, 0, values);

            assert position == string.length();
            assert values.size() == 1;
            assert values.get(0) instanceof Multiple;

            return (Multiple)values.get(0);
        }

        public static int parse(String string, int position, List<Value> values) {
            var result = new ArrayList<Value>();
            position++;

            boolean consumeMore = true;

            while (consumeMore) {
                var charAtPosition = string.charAt(position);

                if (charAtPosition == '[') {
                    position = Multiple.parse(string, position, result);
                } else if (charAtPosition >= '0' && charAtPosition <= '9') {
                    position = Single.parse(string, position, result);
                } else if (charAtPosition == ']') {
                    position++;
                    consumeMore = false;
                } else if (charAtPosition == ',') {
                    position++;
                } else {
                    throw new IllegalStateException("Unexpected input: " + charAtPosition + " at " + position + "\n" + string + "\n" + " ".repeat(position) + "^");
                }
            }

            values.add(new Multiple(result));

            return position;
        }

        @Override
        public int compareTo(Value v) {
            return compareValues(this, v);
        }
    }

    record PacketPair(Multiple left, Multiple right) {
        static PacketPair parse(String string) {
            var packets = Arrays.stream(string.split("\n")).map(Multiple::parse).toList();
            return new PacketPair(packets.get(0), packets.get(1));
        }
    }

    @Override
    public Assignment first() {
        return input -> {
            var packetPairs = parse(input);

            return IntStream.range(0, packetPairs.size())
                    .filter(i -> compareValues(packetPairs.get(i).left, packetPairs.get(i).right) <= 0)
                    .map(i -> i + 1)
                    .sum();
        };
    }

    @Override
    public Assignment second() {
        return input -> {
            var packetPairs = parse(input);

            var decoderKey = PacketPair.parse("""
                    [[2]]
                    [[6]]""");

            var sortedPackets = Stream.concat(Stream.of(decoderKey), packetPairs.stream())
                    .flatMap(p -> Stream.of(p.left, p.right))
                    .sorted()
                    .toList();

            return IntStream.range(0, sortedPackets.size())
                    .filter(i -> Stream.of(decoderKey.left, decoderKey.right).anyMatch(k -> compareValues(sortedPackets.get(i), k) == 0))
                    .map(i -> i + 1)
                    .reduce((a, b) -> a * b)
                    .orElseThrow();
        };
    }

    private static List<PacketPair> parse(String input) {
        return Arrays.stream(input
            .split("\\n\\n"))
            .map(PacketPair::parse)
            .toList();
    }
}
