package nl.q8p.aoc2022.day24;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.logging.Logger;

public class Day24 implements Day {

    private static final Logger LOG = Logger.getLogger(Day24.class.getName());

    enum Direction {
        UP(0, -1, 1, '^'),
        DOWN(0, 1, 2, 'v'),
        LEFT(-1, 0, 4, '<'),
        RIGHT(1, 0, 8, '>');

        final int deltaX;
        final int deltaY;

        final int bit;
        final char representation;

        boolean isPresent(int value) {
            return (value & bit) == bit;
        }

        Direction(int deltaX, int deltaY, int bit, char representation) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
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

    enum Move {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);


        private final int deltaX;
        private final int deltaY;

        Move(int deltaX, int deltaY) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
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
        int[] blizzards;

        final int width;

        final int height;

        final int entry;

        final int exit;

        Valley(int[] blizzards, int height, int entry, int exit) {
            this.blizzards = blizzards;
            this.height = height;
            this.entry = entry;
            this.exit = exit;

            this.width = blizzards.length / height;
        }

        private int timeWalking(boolean reversed) {
            var start = reversed ? new Position(exit, height) : new Position(entry, -1);
            var finish = reversed ? new Position(entry, -1) : new Position(exit, height);

            var possible = new HashSet<Position>();
            possible.add(start);

            var time = 0;
            do {
                tick();

                var positionsToAdd = new HashSet<Position>();

                for (var position : possible) {
                    for (var move : Move.values()) {
                        var newX = position.x + move.deltaX;
                        var newY = position.y + move.deltaY;

                        if (isFree(newX, newY)) {
                            positionsToAdd.add(new Position(newX, newY));
                        }
                    }
                }

                possible.addAll(positionsToAdd);

                for (var x = 0; x < width; x++) {
                    for (var y = 0; y < height; y++) {
                        if (!isFree(x, y)) {
                            possible.remove(new Position(x, y));
                        }
                    }
                }

                time++;
            } while (!possible.contains(finish));
            return time;
        }

        void tick() {
            var newBlizzards = new int[width * height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    var value = blizzards[y * width + x];

                    for (var direction : Direction.values()) {
                        if (direction.isPresent(value)) {
                            int newX = (x + direction.deltaX + width) % width;
                            int newY = (y + direction.deltaY + height) % height;

                            newBlizzards[newY * width + newX] |= direction.bit;
                        }
                    }
                }
            }

            blizzards = newBlizzards;
        }

        static Valley parse(String string) {
            var lines = string.split("\n");
            var width = lines[0].length() - 2;
            var height = lines.length - 2;

            var blizzards = new int[height * width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    var representation = lines[y + 1].charAt(x + 1);

                    var direction = Direction.parse(representation);

                    if (direction.isPresent()) {
                        blizzards[y * width + x] = direction.get().bit;
                    }
                }
            }

            var entryX = lines[0].indexOf('.') - 1;
            var exitX = lines[lines.length - 1].indexOf('.') - 1;

            return new Valley(blizzards, height, entryX, exitX);
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();

            builder.append(rowWithGapAtX(entry));
            builder.append('\n');

            for (var y = 0; y < height; y++) {
                builder.append('#');

                for (var x = 0; x < width; x++) {
                    var value = blizzards[y * width + x];

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

            builder.append(rowWithGapAtX(exit));
            builder.append('\n');

            return builder.toString();
        }

        private String rowWithGapAtX(int x) {
            var top = new StringBuilder("#".repeat(width + 2));
            top.setCharAt(x + 1, '.');
            return top.toString();
        }

        public boolean isFree(int newX, int newY) {
            if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                return blizzards[newY * width + newX] == 0;
            } else return (newY == height && newX == exit) || (newY == -1 && newX == entry);
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> Valley.parse(input).timeWalking(false);
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var valley = Valley.parse(input);

            int time1 = valley.timeWalking(false);
            int time2 = valley.timeWalking(true);
            int time3 = valley.timeWalking(false);

            return time1 + time2 + time3;
        };
    }
}
