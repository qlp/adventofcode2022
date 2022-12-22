package nl.q8p.aoc2022.day22;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import static nl.q8p.aoc2022.day22.Day22.SideType.BOTTOM;
import static nl.q8p.aoc2022.day22.Day22.SideType.DOWN;
import static nl.q8p.aoc2022.day22.Day22.SideType.LEFT;
import static nl.q8p.aoc2022.day22.Day22.SideType.RIGHT;
import static nl.q8p.aoc2022.day22.Day22.SideType.TOP;
import static nl.q8p.aoc2022.day22.Day22.SideType.UP;
import static nl.q8p.aoc2022.day22.Day22.TileType.EMPTY;
import static nl.q8p.aoc2022.day22.Day22.TileType.OPEN;
import static nl.q8p.aoc2022.day22.Day22.TileType.WALL;

public class Day22 implements Day {

    private static final Logger LOG = Logger.getLogger(Day22.class.getName());

    record Cursor(Position position, Orientation orientation) {
        Cursor with(Position withPosition) {
            return new Cursor(withPosition, orientation);
        }

        @Override
        public String toString() {
            return "" + position.x + ", " + position.y + " " + orientation.name();
        }
    }

    enum Orientation {
        UP(0, -1, 1, 3, 3),
        RIGHT(1, 0, 2, 0, 0),
        DOWN(0, 1, 3, 1, 1),
        LEFT(-1, 0, 0, 2, 2);

        final int moveX;
        final int moveY;

        final int turnRightOrientationIndex;

        final int turnLeftOrientationIndex;

        final int score;

        Orientation turnRight() {
            return Orientation.values()[turnRightOrientationIndex];
        }

        Orientation turnLeft() {
            return Orientation.values()[turnLeftOrientationIndex];
        }

        Orientation(int moveX, int moveY, int turnRight, int turnLeft, int score) {
            this.moveX = moveX;
            this.moveY = moveY;
            this.turnRightOrientationIndex = turnRight;
            this.turnLeftOrientationIndex = turnLeft;
            this.score = score;
        }
    }

    record Position(int x, int y) { }

    enum TileType {
        EMPTY, OPEN, WALL
    }

    sealed interface Operation permits Move, Turn {
        Cursor apply(Board board, Cursor cursor, MoveLogic moveLogic);
    }

    record Move(int numberOfSteps) implements Operation {

        @Override
        public Cursor apply(Board board, Cursor cursor, MoveLogic moveLogic) {
            return moveLogic.move(board, cursor, numberOfSteps);
        }

        @Override
        public String toString() {
            return "" + numberOfSteps;
        }
    }

    record Turn(Direction direction) implements Operation {

        @Override
        public Cursor apply(Board board, Cursor cursor, MoveLogic moveLogic) {
            var newOrientation = switch (direction) {
                case LEFT -> cursor.orientation.turnLeft();
                case RIGHT -> cursor.orientation.turnRight();
            };

            return new Cursor(cursor.position, newOrientation);
        }

        @Override
        public String toString() {
            return switch (direction) {
                case RIGHT -> "R";
                case LEFT -> "L";
            };
        }
    }

    enum Direction {
        LEFT, RIGHT
    }

    interface MoveLogic {
        public Cursor move(Board board, Cursor from, int steps);
    }

    static class FirstMoveLogic implements MoveLogic{
        final Map<Integer, Integer> minYforX;
        final Map<Integer, Integer> maxYforX;

        final Map<Integer, Integer> minXforY;
        final Map<Integer, Integer> maxXforY;

        FirstMoveLogic(Board board) {
            minYforX = new HashMap<>();
            maxYforX = new HashMap<>();

            for (int x = 0; x < board.width; x++) {
                var minY = board.height - 1;
                var maxyY = 0;

                for (int y = 0; y < board.height; y++) {
                    if (board.tiles[y][x] != EMPTY) {
                        if (y < minY) {
                            minY = y;
                        }
                        if (y > maxyY) {
                            maxyY = y;
                        }
                    }
                }
                minYforX.put(x, minY);
                maxYforX.put(x, maxyY);
            }

            minXforY = new HashMap<>();
            maxXforY = new HashMap<>();

            for (int y = 0; y < board.height; y++) {
                var minX = board.width - 1;
                var maxX = 0;

                for (int x = 0; x < board.width; x++) {
                    if (board.tiles[y][x] != EMPTY) {
                        if (x < minX) {
                            minX = x;
                        }
                        if (x > maxX) {
                            maxX = x;
                        }
                    }
                }
                minXforY.put(y, minX);
                maxXforY.put(y, maxX);
            }
        }

        public Cursor move(Board board, Cursor from, int steps) {
            return switch(from.orientation) {
                case RIGHT -> moveRight(board, from, steps);
                case LEFT -> moveLeft(board, from, steps);
                case DOWN -> moveDown(board, from, steps);
                case UP -> moveUp(board, from, steps);
            };
        }

        private Cursor moveRight(Board board, Cursor from, int times) {
            if (times == 0) {
                return from;
            }

            var candidate = from.with(new Position((from.position.x + 1) % board.width, from.position.y));
            var tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];

            if (tileAtCandidate == EMPTY) {
                candidate = from.with(new Position(minXforY.get(candidate.position.y), candidate.position.y));
                tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];
            }

            return switch (tileAtCandidate) {
                case OPEN -> moveRight(board, candidate, times - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + candidate);
            };
        }

        private Cursor moveLeft(Board board, Cursor from, int times) {
            if (times == 0) {
                return from;
            }

            var candidate = from.with(new Position((from.position.x - 1 + board.width) % board.width, from.position.y));
            var tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];

            if (tileAtCandidate == EMPTY) {
                candidate = from.with(new Position(maxXforY.get(candidate.position.y), candidate.position.y));
                tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];
            }

            return switch (tileAtCandidate) {
                case OPEN -> moveLeft(board, candidate, times - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + candidate);
            };
        }

        private Cursor moveDown(Board board, Cursor from, int times) {
            if (times == 0) {
                return from;
            }

            var candidate = from.with(new Position(from.position.x, (from.position.y + 1) % board.height));
            var tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];

            if (tileAtCandidate == EMPTY) {
                candidate = from.with(new Position(candidate.position.x, minYforX.get(candidate.position.x)));
                tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];
            }

            return switch (tileAtCandidate) {
                case OPEN -> moveDown(board, candidate, times - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + candidate);
            };
        }

        private Cursor moveUp(Board board, Cursor from, int times) {
            if (times == 0) {
                return from;
            }

            var candidate = from.with(new Position(from.position.x, (from.position.y - 1 + board.height) % board.height));
            var tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];

            if (tileAtCandidate == EMPTY) {
                candidate = from.with(new Position(candidate.position.x, maxYforX.get(candidate.position.x)));
                tileAtCandidate = board.tiles[candidate.position.y][candidate.position.x];
            }

            return switch (tileAtCandidate) {
                case OPEN -> moveUp(board, candidate, times - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + candidate);
            };
        }
    }

    record SideCoordinate(int x, int y) { }

    enum SideType {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
        UP,
        DOWN;
    }

    enum ConnectionType {
        ROOT, ABOVE, LEFT, RIGHT
    }

    record Side(SideType sideType, Destination up, Destination left, Destination down, Destination right) {

    }

    record LocalCoordinate(int x, int y) {
        public LocalCoordinate transform(Transform transform, LocalCoordinate from, int tileSize) {
            return switch (transform) {
                case FLIP_X -> new LocalCoordinate(tileSize - 1 - from.x, from.y);
                case FLIP_Y -> new LocalCoordinate(x, tileSize - 1 - from.y);
                case X_TO_Y, Y_TO_X -> new LocalCoordinate(y, x);
                case FLIP_Y_TO_FLIP_X, FLIP_X_TO_Y -> new LocalCoordinate(tileSize - 1- from.y, tileSize - 1- from.x);
            };
        }
    }

    record TilePosition(int x, int y) { }

    static class CubeLayout {
        final Side[][] sides;

        CubeLayout(Side[][] sides) {
            this.sides = sides;
        }

        Cursor when(Board board, Cursor cursor) {
            var from = find(board, cursor);

            var to = switch (cursor.orientation) {
                case UP -> from.up;
                case LEFT -> from.left;
                case DOWN -> from.down;
                case RIGHT -> from.right;
            };

            return transform(board, cursor, to);
        }

        Cursor transform(Board board, Cursor cursor, Destination destination) {
            var orientation = destination.orientation;

            int tileSize = board.width / sides[0].length;
            var local = new LocalCoordinate(cursor.position.x % tileSize, cursor.position.y % tileSize);

            var localTransformed = local.transform(destination.transform, local, tileSize);

            var tilePosition = tilePosition(destination.to);

            var global = new Position(tilePosition.x * tileSize + localTransformed.x, tilePosition.y * tileSize + localTransformed.y);

            return new Cursor(global, orientation);
        }

        Side find(Board board, Cursor cursor) {
            return sides[cursor.position.y / (board.height / sides.length)][cursor.position.x / (board.width / sides[0].length)];
        }

        TilePosition tilePosition(SideType sideType) {
            for (int y = 0; y < sides.length; y++) {
                for (int x = 0; x < sides[y].length; x++) {
                    if (sides[y][x] != null && sides[y][x].sideType == sideType) {
                        return new TilePosition(x, y);
                    }
                }
            }

            throw new IllegalStateException("side not found: " + sideType);
        }
    }

//    enum Moving {
//        LEFT(-1, 0),
//        RIGHT(1, 0),
//        TOP(0, 1),
//        BOTTOM(0, -1);
//
//        final int x;
//        final int y;
//
//        Moving(int x, int y) {
//            this.x = x;
//            this.y = y;
//        }
//    }

    static class SecondMoveLogic implements MoveLogic{

        private final CubeLayout cubeLayout;

        SecondMoveLogic(CubeLayout cubeLayout) {
            this.cubeLayout = cubeLayout;
        }

        public Cursor move(Board board, Cursor from, int steps) {
            if (steps == 0) {
                return from;
            }

            var candidate = from.with(new Position(from.position.x + from.orientation.moveX, from.position.y + from.orientation.moveY));

            var tileAtCandidate = board.get(candidate.position);

            if (tileAtCandidate == EMPTY) {
                candidate = cubeLayout.when(board, from);
                tileAtCandidate = board.get(candidate.position);
            }

            return switch (tileAtCandidate) {
                case OPEN -> move(board, candidate, steps - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + candidate);
            };
        }
    }

    static class Board {

        final TileType[][] tiles;

        final int width;
        final int height;

        Board(TileType[][] tiles) {
            this.tiles = tiles;
            this.height = tiles.length;
            this.width = tiles[0].length;
        }

        public TileType get(Position position) {
            if ((position.x < 0) || (position.x >= width) || (position.y < 0) || (position.y >= height)) {
                return EMPTY;
            }
            return tiles[position.y][position.x];
        }

        public Position leftmostOpenTileOfTheTopRowOfTiles() {
            return new Position(IntStream.range(0, width).filter(x -> tiles[0][x] == OPEN).findFirst().orElseThrow(), 0);
        }

        @Override
        public String toString() {
            return String.join("\n", Arrays.stream(tiles).map(line -> String.join("", Arrays.stream(line).map(t -> switch (t) {
                case EMPTY -> " ";
                case WALL -> "#";
                case OPEN -> ".";
            }).toList())).toList());
        }

        static Board parse(String string) {
            var lines = string.split("\\n");
            var width = Arrays.stream(lines).mapToInt(String::length).max().orElseThrow();

            var tiles = Arrays.stream(lines).map(l -> {
                    var tileLine = new TileType[width];
                    for (int i = 0; i < width; i++) {
                        var tileChar = l.length() > i ? l.charAt(i) : ' ';
                        var tile = switch (tileChar) {
                            case '.' -> OPEN;
                            case '#' -> WALL;
                            case ' ' -> EMPTY;
                            default -> throw new IllegalStateException("no char for " + i + " in " + l);
                        };

                        tileLine[i] = tile;
                    }

                    return tileLine;
                }).toArray(TileType[][]::new);

            return new Board(tiles);
        }
    }

    record Operations(List<Operation> list) {
        static Operations parse(String string) {
            var result = new ArrayList<Operation>();

            var index = 0;
            var buffer = new StringBuilder();
            while (index < string.length()) {
                var charAtIndex = string.charAt(index);

                switch (charAtIndex) {
                    case 'L' -> {
                        if (!buffer.isEmpty()) {
                            result.add(new Move(Integer.parseInt(buffer.toString())));
                            buffer = new StringBuilder();
                        }
                        result.add(new Turn(Direction.LEFT));
                    }
                    case 'R' -> {
                        if (!buffer.isEmpty()) {
                            result.add(new Move(Integer.parseInt(buffer.toString())));
                            buffer = new StringBuilder();
                        }
                        result.add(new Turn(Direction.RIGHT));
                    }
                    default -> buffer.append(charAtIndex);
                }

                index++;
            }
            result.add(new Move(Integer.parseInt(buffer.toString())));

            return new Operations(result);
        }

        @Override
        public String toString() {
            return String.join("", list.stream().map(Object::toString).toList());
        }
    }

    record Scenario(Board board, Operations operations) {
        static Scenario parse(String string) {
            var segments = string.split("\\n\\n");
            return new Scenario(
                Board.parse(segments[0]),
                Operations.parse(segments[1])
            );
        }

        public Cursor playFrom(Cursor begin, MoveLogic moveLogic) {
            var result = begin;
            for(var operation : operations.list) {
                result = operation.apply(board, result, moveLogic);
            }
            return result;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var scenario = Scenario.parse(input);

            var begin = new Cursor(scenario.board().leftmostOpenTileOfTheTopRowOfTiles(), Orientation.RIGHT);

            var end = scenario.playFrom(begin, new FirstMoveLogic(scenario.board));

            return (end.position.y + 1) * 1000 + (end.position.x + 1) * 4 + end.orientation.score;
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var scenario = Scenario.parse(input);

            var begin = new Cursor(scenario.board().leftmostOpenTileOfTheTopRowOfTiles(), Orientation.RIGHT);

            var end = scenario.playFrom(begin, new SecondMoveLogic(cubeLayout(run)));

            return (end.position.y + 1) * 1000 + (end.position.x + 1) * 4 + end.orientation.score;
        };
    }

    enum Transform {
        Y_TO_X, FLIP_Y, X_TO_Y, FLIP_X_TO_Y, FLIP_Y_TO_FLIP_X, FLIP_X

    }

    record Destination(SideType to, Orientation orientation, Transform transform) { }

    public CubeLayout cubeLayout(Assignment.Run run) {
        return switch (run) {
            case EXAMPLE -> exampleLayout();
            case REAL -> realLayout();
        };
    }

    public CubeLayout exampleLayout() {
        // 1
        var top = new Side(TOP,
                new Destination(UP, Orientation.DOWN, Transform.FLIP_X),
                new Destination(LEFT, Orientation.DOWN, Transform.Y_TO_X),
                new Destination(DOWN, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(RIGHT, Orientation.LEFT, Transform.FLIP_Y));
        // 6
        var up = new Side(UP,
                new Destination(TOP, Orientation.DOWN, Transform.FLIP_X),
                new Destination(RIGHT, Orientation.UP, Transform.FLIP_Y_TO_FLIP_X),
                new Destination(BOTTOM, Orientation.UP, Transform.FLIP_X),
                new Destination(LEFT, Orientation.RIGHT, Transform.FLIP_X));
        // 5
        var left = new Side(LEFT,
                new Destination(TOP, Orientation.RIGHT, Transform.X_TO_Y),
                new Destination(UP, Orientation.LEFT, Transform.FLIP_X),
                new Destination(BOTTOM, Orientation.RIGHT, Transform.FLIP_X_TO_Y),
                new Destination(DOWN, Orientation.RIGHT, Transform.FLIP_X));
        // 2
        var down = new Side(DOWN,
                new Destination(TOP, Orientation.UP, Transform.FLIP_Y),
                new Destination(LEFT, Orientation.LEFT, Transform.FLIP_X),
                new Destination(BOTTOM, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(RIGHT, Orientation.DOWN, Transform.FLIP_Y_TO_FLIP_X));
        // 3
        var bottom = new Side(BOTTOM,
                new Destination(DOWN, Orientation.UP, Transform.FLIP_Y),
                new Destination(LEFT, Orientation.UP, Transform.FLIP_Y_TO_FLIP_X),
                new Destination(UP, Orientation.UP, Transform.FLIP_X),
                new Destination(RIGHT, Orientation.RIGHT, Transform.FLIP_X));
        // 4
        var right = new Side(RIGHT,
                new Destination(DOWN, Orientation.LEFT, Transform.FLIP_X_TO_Y),
                new Destination(BOTTOM, Orientation.LEFT, Transform.FLIP_X),
                new Destination(UP, Orientation.RIGHT, Transform.FLIP_X_TO_Y),
                new Destination(TOP, Orientation.LEFT, Transform.FLIP_Y));

        return new CubeLayout(new Side[][] {
                new Side[] { null, null, top, null },
                new Side[] { up, left, down, null },
                new Side[] { null, null, bottom, right }
        });

    }

    public CubeLayout realLayout() {
//        var top = new Side(SideType.TOP);
//        var up = new Side(SideType.UP);
//        var left = new Side(SideType.LEFT);
//        var down = new Side(SideType.DOWN);
//        var bottom = new Side(SideType.BOTTOM);
//        var right = new Side(SideType.RIGHT);
//
//        return new CubeLayout(new Side[][] {
//                new Side[] { null, null, top, null },
//                new Side[] { up, left, down, null },
//                new Side[] { null, null, bottom, right }
//        });

        return null;
    }
}
