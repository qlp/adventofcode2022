package nl.q8p.aoc2022.day23;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day23 implements Day {

    enum Direction {
        UP(Move.N, Move.NE, Move.NW),
        DOWN(Move.S, Move.SE, Move.SW),
        LEFT(Move.W, Move.NW, Move.SW),
        RIGHT(Move.E, Move.NE, Move.SE);

        final Move target;
        final EnumSet<Move> checks;

        List<Direction> consider() {
            var values = values();
            var count = values.length;
            var index = IntStream.range(0, count).filter(i -> values[i] == this).findFirst().orElseThrow();

            var result = new ArrayList<Direction>();

            for (int i = 0; i < count; i++) {
                result.add(values[(i + index) % count]);
            }

            return result;
        }

        Position move(Position position) {
            return position.move(target);
        }

        public Direction next() {
            var values = values();
            var count = values.length;
            var index = IntStream.range(0, count).filter(i -> values[i] == this).findFirst().orElseThrow();

            return values[(index + 1) % count];
        }

        Direction(Move target, Move diagonal1, Move diagonal2) {
            this.target = target;
            this.checks = EnumSet.of(target, diagonal1, diagonal2);
        }
    }

    enum Move {
        N(0, -1),
        NE(1, -1),
        E(1, 0),
        SE(1, 1),
        S(0, 1),
        SW(-1, 1),
        W(-1, 0),
        NW(-1, -1);

        final long x;
        final long y;

        Move(long x, long y) {
            this.x = x;
            this.y = y;
        }
    }

    record Position(long x, long y) {

        @Override
        public String toString() {
            return "" + x + ", " + y;
        }

        public Position move(Move move) {
            return new Position(x + move.x, y + move.y);
        }

        Set<Position> around() {
            return Arrays.stream(Move.values()).map(this::move).collect(Collectors.toSet());
        }
    }

    record Area(Position min, Position max) {
        long width() {
            return max.x - min.x + 1;
        }

        long height() {
            return max.y - min.y + 1;
        }

        long surface() {
            return width() * height();
        }

        @Override
        public String toString() {
            return  min.toString() + " - " + max.toString() + " (" + width() + " x " + height() + " = " + surface() + ")";
        }
    }

    static class World {
        final Set<Position> elves;

        final Direction direction;

        World(Set<Position> elves, Direction direction) {
            this.elves = elves;
            this.direction = direction;
        }

        Area area() {
            var minX = elves.stream().mapToLong(elve -> elve.x).min().orElseThrow();
            var maxX = elves.stream().mapToLong(elve -> elve.x).max().orElseThrow();

            var minY = elves.stream().mapToLong(elve -> elve.y).min().orElseThrow();
            var maxY = elves.stream().mapToLong(elve -> elve.y).max().orElseThrow();

            return new Area(
                new Position(minX, minY),
                new Position(maxX, maxY)
            );
        }

        boolean exists(Position position) {
            return elves.contains(position);
        }

        long emptyTiles() {
            return area().surface() - elves.size();
        }

        public String toString() {
            var result = new StringBuilder();

            var area = area();

            result.append(area.toString() + ": direction: " + direction + "\n");

            for (long y = area.min.y; y <= area.max.y; y++) {
                for (long x = area.min.x; x <= area.max.x; x++) {
                    if (exists(new Position(x, y))) {
                        result.append('#');
                    } else {
                        result.append('.');
                    }
                }
                result.append("\n");
            }

            return result.toString();
        }

        static World parse(String string) {
            var lines = Arrays.stream(string.split("\\n")).toList();

            Set<Position> elves = new HashSet<>();

            for (int y = 0; y < lines.size(); y++) {
                var line = lines.get(y);
                for (int x = 0; x < line.length(); x++) {
                    if (line.charAt(x) == '#') {
                        elves.add(new Position(x, y));
                    }
                }
            }

            return new World(elves, Direction.UP);
        }

        public long firstRoundWithoutMove() {
            int round = 1;

            var previousWorld = this;
            while(true) {
                var newWorld = previousWorld.tick();

                if (newWorld.elves.equals(previousWorld.elves)) {
                    break;
                }
                previousWorld = newWorld;
                round++;
            }

            return round;
        }

        public World tick(int times) {
            if (times == 0) {
                return this;
            }

            var newWorld = tick();

            return newWorld.tick(times - 1);
        }

        Position next(Position from, List<Direction> consider) {
            Position result = null;

            if (from.around().stream().noneMatch(elves::contains)) {
                return from;
            }

            for (var candidate : consider) {
                var checks = candidate.checks.stream().map(from::move).collect(Collectors.toSet());

                if (checks.stream().noneMatch(elves::contains)) {
                    result = from.move(candidate.target);
                    break;
                }
            }

            if (result == null) {
                result = from;
            }

            return result;
        }

        World tick() {
            var consider = direction.consider();
            var currentToNext = new ConcurrentHashMap<Position, Position>();
            var targetCount = new ConcurrentHashMap<Position, Integer>();

            elves.stream().parallel().forEach(current -> {
                var next = next(current, consider);
                currentToNext.put(current, next);
                targetCount.put(next, targetCount.getOrDefault(next, 0) + 1);
            });

            var result = elves.stream().parallel().map(current -> {
                var next = currentToNext.get(current);

                if (targetCount.get(next) == 1) {
                    return next;
                } else {
                    return current;
                }
            }).collect(Collectors.toSet());

            return new World(result, direction.next());
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> World.parse(input).tick(10).emptyTiles();
    }

    @Override
    public Assignment second() {
        return (run, input) -> World.parse(input).firstRoundWithoutMove();
    }
}
