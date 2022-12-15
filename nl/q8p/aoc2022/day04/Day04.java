package nl.q8p.aoc2022.day04;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import static java.util.Arrays.stream;

public class Day04 implements Day {

    record SectionRange(int from, int until) {
        static SectionRange parse(String string) {
            var parts = stream(string.split("-"))
                .mapToInt(Integer::parseInt)
                .toArray();

            return new SectionRange(parts[0], parts[1]);
        }

        boolean contains(int section) {
            return from <= section && until >= section;
        }

        boolean contains(SectionRange other) {
            return contains(other.from) && contains(other.until);
        }

        boolean overlaps(SectionRange other) {
            return contains(other.from) || contains(other.until) || other.contains(from) || other.contains(until);
        }
    }

    record Pair(SectionRange left, SectionRange right) {
        static Pair parse(String string) {
            var sections = stream(string.split(","))
                .map(SectionRange::parse)
                .toList();

            return new Pair(sections.get(0), sections.get(1));
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> stream(input.split("\\n"))
            .map(Pair::parse)
            .filter(p -> p.left.contains(p.right) || p.right.contains(p.left))
            .count();
    }

    @Override
    public Assignment second() {
        return (run, input) -> stream(input.split("\\n"))
            .map(Pair::parse)
            .filter(p -> p.left.overlaps(p.right))
            .count();
    }
}
