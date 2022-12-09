package nl.q8p.aoc2022.day09;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.IntStream;

public class Day09 implements Day {

    record Position(int row, int column) {
        Position move(Direction direction) {
            return new Position(row + direction.horizontal, column + direction.vertical);
        }

        Direction direction(Position other) {
            return new Direction(row - other.row, column - other.column);
        }
    }

    record Plank(Position head, Position tail) {
        Plank() {
            this(new Position(0, 0), new Position(0, 0));
        }

        Plank move(Direction direction) {
            var newHead = head.move(direction);

            var directionToNewHead = newHead.direction(tail);

            var newTail = tail;
            if (Math.abs(directionToNewHead.horizontal) > 1 || Math.abs(directionToNewHead.vertical) > 1) {
                newTail = head;
            }

            return new Plank(newHead, newTail);
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
        return input -> {
            var history = new LinkedList<Plank>();
            history.add(new Plank());

            for (String line : input.split("\\n")) {
                String[] parts = line.split(" ");
                var direction = Move.valueOf(parts[0]).direction;
                var steps = Integer.parseInt(parts[1]);

                IntStream.range(0, steps).forEach(s -> history.add(history.getLast().move(direction)));
            }

            return history.stream().map(Plank::tail).distinct().count();
        };
    }

    @Override
    public Assignment second() {
        return input -> "";
    }
}
