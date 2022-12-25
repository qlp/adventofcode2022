package nl.q8p.aoc2022.day23;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day23 implements Day {
    private static final Logger LOG = Logger.getLogger(Day23.class.getName());

    enum Direction {
        UP(Move.N, Move.NE, Move.NW),
        DOWN(Move.S, Move.SE, Move.SW),
        LEFT(Move.W, Move.NW, Move.SW),
        RIGHT(Move.E, Move.NE, Move.SE);

        final Move target;
        final EnumSet<Move> checks;

        final int mask;

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

        public Direction next() {
            var values = values();
            var count = values.length;
            var index = IntStream.range(0, count).filter(i -> values[i] == this).findFirst().orElseThrow();

            return values[(index + 1) % count];
        }

        Direction(Move target, Move diagonal1, Move diagonal2) {
            this.target = target;
            this.checks = EnumSet.of(target, diagonal1, diagonal2);
            this.mask = checks.stream().mapToInt(move -> move.bit).reduce(0, (a, b) -> a | b);
        }
    }

    static final int N_BIT = 1;
    static final int E_BIT = 4;
    static final int S_BIT = 16;
    static final int W_BIT = 64;

    enum Move {
        N(0, -1, N_BIT),
        NE(1, -1, 2),
        E(1, 0, E_BIT),
        SE(1, 1, 8),
        S(0, 1, S_BIT),
        SW(-1, 1, 32),
        W(-1, 0, W_BIT),
        NW(-1, -1, 128);

        final int x;
        final int y;
        final int bit;

        Move(int x, int y, int bit) {
            this.x = x;
            this.y = y;
            this.bit = bit;
        }
    }

    record Position(int x, int y) {

        @Override
        public String toString() {
            return "" + x + ", " + y;
        }

        public Position move(Move move) {
            return new Position(x + move.x, y + move.y);
        }

        public Position from(Move move) {
            return new Position(x - move.x, y - move.y);
        };
    }

    record Area(Position min, Position max) {
        int width() {
            return max.x - min.x + 1;
        }

        int height() {
            return max.y - min.y + 1;
        }

        int surface() {
            return width() * height();
        }

        @Override
        public String toString() {
            return  min.toString() + " - " + max.toString() + " (" + width() + " x " + height() + " = " + surface() + ")";
        }
    }

    static class World {
        final Set<Position> elves;

        Direction direction;

        World(Set<Position> elves, Direction direction) {
            this.elves = elves;
            this.direction = direction;
        }

        Area area() {
            var minX = elves.stream().mapToInt(elve -> elve.x).min().orElseThrow();
            var maxX = elves.stream().mapToInt(elve -> elve.x).max().orElseThrow();

            var minY = elves.stream().mapToInt(elve -> elve.y).min().orElseThrow();
            var maxY = elves.stream().mapToInt(elve -> elve.y).max().orElseThrow();

            return new Area(
                new Position(minX, minY),
                new Position(maxX, maxY)
            );
        }

        boolean exists(Position position) {
            return elves.contains(position);
        }

        int emptyTiles() {
            return area().surface() - elves.size();
        }

        public String toString() {
            var result = new StringBuilder();

            var area = area();

            result.append(area.toString() + ": direction: " + direction + "\n");

            for (int y = area.min.y; y <= area.max.y; y++) {
                for (int x = area.min.x; x <= area.max.x; x++) {
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

        public int firstRoundWithoutMove() {
            int round = 1;

            while(tick()) {
                round++;
            }

            return round;
        }

        public World tick(int times) {
            for (int time = 0; time < times; time++) {
                tick();
            }

            return this;
        }

        record PositionMove(Position position, Move move) { }

        Optional<PositionMove> next(Position from, List<Direction> consider) {
            int around = 0;
            around |= elves.contains(new Position(from.x + Move.N.x, from.y + Move.N.y)) ? Move.N.bit : 0;
            around |= elves.contains(new Position(from.x + Move.NE.x, from.y + Move.NE.y)) ? Move.NE.bit : 0;
            around |= elves.contains(new Position(from.x + Move.E.x, from.y + Move.E.y)) ? Move.E.bit : 0;
            around |= elves.contains(new Position(from.x + Move.SE.x, from.y + Move.SE.y)) ? Move.SE.bit : 0;
            around |= elves.contains(new Position(from.x + Move.S.x, from.y + Move.S.y)) ? Move.S.bit : 0;
            around |= elves.contains(new Position(from.x + Move.SW.x, from.y + Move.SW.y)) ? Move.SW.bit : 0;
            around |= elves.contains(new Position(from.x + Move.W.x, from.y + Move.W.y)) ? Move.W.bit : 0;
            around |= elves.contains(new Position(from.x + Move.NW.x, from.y + Move.NW.y)) ? Move.NW.bit : 0;

            if (around == 0) {
                return Optional.empty();
            }

            for (var candidate : consider) {
                if ((around & candidate.mask) == 0) {
                    return Optional.of(new PositionMove(from.move(candidate.target), candidate.target));
                }
            }

            return Optional.empty();
        }

        boolean tick() {
            var consider = direction.consider();
            var nextToMoves = new ConcurrentHashMap<Position, Integer>();

            elves.stream().parallel().forEach(current -> {
                var next = next(current, consider);
                next.ifPresent(n -> nextToMoves.compute(n.position, (k, v) -> v == null ? n.move.bit : v | n.move.bit));
            });

            var changed = new AtomicBoolean(false);

            nextToMoves.forEach((next, moves) -> {
                var move = switch (moves) {
                    case N_BIT -> Move.N;
                    case E_BIT -> Move.E;
                    case S_BIT -> Move.S;
                    case W_BIT -> Move.W;
                    default -> null;
                };

                if (move != null) {
                    changed.set(true);
                    var from = next.from(move);
                    elves.remove(from);
                    elves.add(next);
                }
            });

            direction = direction.next();

            return changed.get();
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
