package nl.q8p.aoc2022.day24;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.Optional;

public class Day24 implements Day {

    enum Direction {
        UP(0, -1, 1, '^'),
        DOWN(0, 1, 2, 'v'),
        LEFT(-1, 0, 4, '<'),
        RIGHT(1, 0, 8, '>');

        int x;
        int y;

        int bit;
        char representation;

        Direction(int deltaX, int deltaY, int bit, char representation) {
            this.x = deltaX;
            this.y = deltaY;
            this.bit = bit;
            this.representation = representation;
        }

        static Optional<Direction> parse(char representation) {
            return Arrays.stream(values()).filter(v -> v.representation == representation).findFirst();
        }

        @Override
        public String toString() {
            return "" + representation;
        }
    }

    record Position(int x, int y) {
        @Override
        public String toString() {
            return "[" + x + ", " + y + "]";
        }
    }

    record Blizzard(Direction direction) { }
    static class Valley {
        final int[][] blizzards;

        final int width;

        final int height;

        final int entryX;

        final int exitX;

        Valley(int[][] blizzards, int entryX, int exitX) {
            this.blizzards = blizzards;
            this.width = blizzards[0].length;
            this.height = blizzards.length;
            this.entryX = entryX;
            this.exitX = exitX;
        }

        static Valley parse(String string) {
            var lines = string.split("\n");
            var width = lines[0].length() - 2;
            var height = lines.length - 2;

            var blizzards = new int[height][];

            for (int y = 0; y < height; y++) {
                blizzards[y] = new int[width];
                for (int x = 0; x < width; x++) {
                    var representation = lines[y + 1].charAt(x + 1);

                    var direction = Direction.parse(representation);

                    if (direction.isPresent()) {
                        blizzards[y][x] = direction.get().bit;
                    }
                }
            }

            var entryX = lines[0].indexOf('.') - 1;
            var exitX = lines[lines.length - 1].indexOf('.') - 1;

            return new Valley(blizzards, entryX, exitX);
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();

            builder.append(rowWithGapAtX(entryX));
            builder.append('\n');

            for (var y = 0; y < height; y++) {
                builder.append('#');

                for (var x = 0; x < width; x++) {
                    var value = blizzards[y][x];

                    var directions = Arrays.stream(Direction.values()).filter(d -> (value & d.bit) == d.bit).toList();

                    var representation = switch (directions.size()) {
                        case 0 -> '.';
                        case 1 -> directions.get(0).representation;
                        default -> (char)('0' + directions.size());
                    };

                    builder.append(representation);
                }

                builder.append('#');
                builder.append('\n');
            }

            builder.append(rowWithGapAtX(exitX));
            builder.append('\n');

            return builder.toString();
        }

        private String rowWithGapAtX(int x) {
            var top = new StringBuilder("#".repeat(width + 2));
            top.setCharAt(x + 1, '.');
            return top.toString();
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var valley = Valley.parse(input);

            return valley.toString();
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
