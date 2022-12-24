package nl.q8p.aoc2022.day24;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.Optional;

import static nl.q8p.aoc2022.day24.Day24.Direction.DOWN;
import static nl.q8p.aoc2022.day24.Day24.Direction.LEFT;
import static nl.q8p.aoc2022.day24.Day24.Direction.RIGHT;
import static nl.q8p.aoc2022.day24.Day24.Direction.UP;

public class Day24 implements Day {
    static final int ME_BIT = 16;

    enum Direction {
        UP(1, '^'),
        DOWN(2, 'v'),
        LEFT(4, '<'),
        RIGHT(8, '>');

        final int bit;
        final char representation;

        Direction(int bit, char representation) {
            this.bit = bit;
            this.representation = representation;
        }

        static Optional<Direction> parse(char representation) {
            return Arrays.stream(values()).filter(v -> v.representation == representation).findFirst();
        }
    }

    static class Valley {
        int[] positions;

        int[] buffer;

        final int width;

        final int height;

        final int entry;

        final int exit;

        Valley(int[] positions, int height, int entry, int exit) {
            this.positions = positions;
            this.height = height;
            this.entry = entry;
            this.exit = exit;

            this.width = positions.length / height;
            this.buffer = new int[positions.length];
        }

        private int timeWalking(boolean reversed) {
            for (int i = 0; i < positions.length; i++) {
                positions[i] &= (LEFT.bit | RIGHT.bit | UP.bit | DOWN.bit);
            }

            var reachedDestination = false;
            var time = 0;
            do {
                reachedDestination = tick(reversed);

                time++;
            } while (!reachedDestination);
            return time;
        }

        boolean tick(boolean reversed) {
            boolean result =
                (reversed && (positions[entry] & ME_BIT) == ME_BIT) ||
                (!reversed && ((positions[(height - 1) * width + exit] & ME_BIT) == ME_BIT));

            for (var y = 0; y < height; y++) {
                for (var x = 0; x < width; x++) {
                    var leftX = (x + width - 1) % width;
                    var rightX = (x + 1) % width;
                    var upY = (y + height - 1) % height;
                    var downY = (y + 1) % height;

                    var receivedLeft = positions[y * width + leftX] & Direction.RIGHT.bit;
                    var receivedRight = positions[y * width + rightX] & LEFT.bit;
                    var receivedDown = positions[downY * width + x] & Direction.UP.bit;
                    var receivedUp = positions[upY * width + x] & Direction.DOWN.bit;

                    var newBlizzardsAtLocation = receivedLeft | receivedRight | receivedUp | receivedDown;

                    if (newBlizzardsAtLocation > 0) {
                        buffer[y * width + x] = newBlizzardsAtLocation;
                    } else {
                        var meStay = (positions[y * width + x] & ME_BIT) == ME_BIT;
                        var meLeft = x != 0 && (positions[y * width + x - 1] & ME_BIT) == ME_BIT;
                        var meRight = x + 1 < width && (positions[y * width + x + 1] & ME_BIT) == ME_BIT;
                        var meDown = y + 1 < height && (positions[(y + 1) * width + x] & ME_BIT) == ME_BIT;
                        var meUp = y > 0 && (positions[(y - 1) * width + x] & ME_BIT) == ME_BIT;

                        var meSpawnAtTop = !reversed && y == 0 && x == entry;
                        var meSpawnAtBottom = reversed && y == (height - 1) && x == exit;

                        var newMeAtLocation = meStay || meLeft || meRight || meUp || meDown || meSpawnAtBottom || meSpawnAtTop;

                        buffer[y * width + x] = newMeAtLocation ? ME_BIT : 0;
                    }
                }
            }

            int[] swap = positions;
            positions = buffer;
            buffer = swap;

            return result;
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
