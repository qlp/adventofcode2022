package nl.q8p.aoc2022.day09;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day09 implements Day {

    record Position(int row, int column) {
        Position move(Direction direction) {
            return new Position(row + direction.vertical, column + direction.horizontal);
        }

        Direction direction(Position other) {
            return new Direction(column - other.column, row - other.row);
        }
    }

    record Plank(List<Position> ropes) {
        Plank(int ropeCount) {
            this(IntStream.range(0, ropeCount).mapToObj(i -> new Position(0, 0)).toList());
        }

        Plank move(Direction direction) {
            var newRopes = new LinkedList<Position>();
            IntStream.range(0, ropes.size()).forEach(i -> {
                Position newRope;
                if (i == 0) {
                    newRope = ropes.get(0).move(direction);
                } else {
                    var newHead = newRopes.get(i - 1);
                    newRope = ropes.get(i);

                    var directionToNewHead = newHead.direction(newRope);
                    if ((directionToNewHead.horizontal >= 1 && directionToNewHead.vertical > 1) || (directionToNewHead.horizontal > 1 && directionToNewHead.vertical == 1)) {
                        newRope = newRope.move(new Direction(1, 1));
                    } else if ((directionToNewHead.horizontal >= 1 && directionToNewHead.vertical < -1) || (directionToNewHead.horizontal > 1 && directionToNewHead.vertical == -1)) {
                        newRope = newRope.move(new Direction(1, -1));
                    } else if ((directionToNewHead.horizontal <= -1 && directionToNewHead.vertical > 1) || (directionToNewHead.horizontal < -1 && directionToNewHead.vertical == 1)) {
                        newRope = newRope.move(new Direction(-1, 1));
                    } else if ((directionToNewHead.horizontal <= -1 && directionToNewHead.vertical < -1) || (directionToNewHead.horizontal < -1 && directionToNewHead.vertical == -1)) {
                        newRope = newRope.move(new Direction(-1, -1));
                    } else if (directionToNewHead.horizontal > 1) {
                        newRope = newRope.move(new Direction(1, 0));
                    } else if (directionToNewHead.horizontal < -1) {
                        newRope = newRope.move(new Direction(-1, 0));
                    } else if (directionToNewHead.vertical > 1) {
                        newRope = newRope.move(new Direction(0, 1));
                    } else if (directionToNewHead.vertical < -1) {
                        newRope = newRope.move(new Direction(0, -1));
                    }
                }

                newRopes.add(newRope);
            });

            return new Plank(newRopes);
        }

        Position tail() {
            return ropes.get(ropes.size() - 1);
        }
    }

    record Direction(int horizontal, int vertical) { }

    enum Move {
        R(new Direction(1, 0)),
        U(new Direction(0, -1)),
        L(new Direction(-1, 0)),
        D(new Direction(0, 1));

        final Direction direction;

        Move(Direction direction) {
            this.direction = direction;
        }
    }

    @Override
    public Assignment first() {
        return input -> tailPositionCount(input, 2);
    }

    @Override
    public Assignment second() {
        return input -> tailPositionCount(input, 10);
    }

    private static long tailPositionCount(String input, int ropeCount) {
        var history = new LinkedList<Plank>();
        history.add(new Plank(ropeCount));

        for (String line : input.split("\\n")) {
            String[] parts = line.split(" ");
            var direction = Move.valueOf(parts[0]).direction;
            var steps = Integer.parseInt(parts[1]);

            IntStream.range(0, steps).forEach(s -> history.add(history.getLast().move(direction)));
        }

        return history.stream().map(Plank::tail).distinct().count();
    }
}
