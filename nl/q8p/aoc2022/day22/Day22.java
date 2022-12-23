package nl.q8p.aoc2022.day22;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    record Cursor(Position position, Orientation orientation) {
        Cursor with(Position withPosition) {
            return new Cursor(withPosition, orientation);
        }

        Cursor next() {
            return new Cursor(new Position(position.x + orientation.moveX, position.y + orientation.moveY), orientation);
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
        Cursor apply(Cursor cursor, MoveLogic moveLogic);
    }

    record Move(int numberOfSteps) implements Operation {

        @Override
        public Cursor apply(Cursor cursor, MoveLogic moveLogic) {
            return moveLogic.move(cursor, numberOfSteps);
        }
    }

    record Turn(Direction direction) implements Operation {

        @Override
        public Cursor apply(Cursor cursor, MoveLogic moveLogic) {
            var newOrientation = switch (direction) {
                case LEFT -> cursor.orientation.turnLeft();
                case RIGHT -> cursor.orientation.turnRight();
            };

            return new Cursor(cursor.position, newOrientation);
        }
    }

    enum Direction {
        LEFT, RIGHT
    }

    interface MoveLogic {

        void init(Board board);

        Cursor move(Cursor from, int steps);
    }

    record Range(int min, int max) {
        Range extend(int value) {
            return new Range(Math.min(min, value), Math.max(max, value));
        }
    }

    static class FirstMoveLogic implements MoveLogic {
        final Map<Integer, Range> rangeForX = new HashMap<>();
        final Map<Integer, Range> rangeForY = new HashMap<>();

        Board board;

        public Cursor move(Cursor from, int times) {
            if (times == 0) {
                return from;
            }

            var to = step(from);

            return switch (board.get(to.position)) {
                case OPEN -> move(to, times - 1);
                case WALL -> from;
                case EMPTY -> throw new IllegalStateException("Did not expect EMPTY at " + to);
            };
        }

        private Cursor step(Cursor from) {
            var next = from.next();

            if (board.get(next.position) == EMPTY) {
                next = switch (from.orientation) {
                    case LEFT -> from.with(new Position(rangeForY.get(next.position.y).max, next.position.y));
                    case RIGHT -> from.with(new Position(rangeForY.get(next.position.y).min, next.position.y));
                    case UP -> from.with(new Position(next.position.x, rangeForX.get(next.position.x).max));
                    case DOWN -> from.with(new Position(next.position.x, rangeForX.get(next.position.x).min));
                };
            }

            return next;
        }

        public void init(Board board) {
            this.board = board;

            if (rangeForX.isEmpty() && rangeForY.isEmpty()) {

                for (int x = 0; x < board.width; x++) {
                    var range = new Range(board.height - 1, 0);

                    for (int y = 0; y < board.height; y++) {
                        if (board.tiles[y][x] != EMPTY) {
                            range = range.extend(y);
                        }
                    }
                    rangeForX.put(x, range);
                }

                for (int y = 0; y < board.height; y++) {
                    var range = new Range(board.width - 1, 0);

                    for (int x = 0; x < board.width; x++) {
                        if (board.tiles[y][x] != EMPTY) {
                            range = range.extend(x);
                        }
                    }
                    rangeForY.put(y, range);
                }
            }
        }
    }

    enum SideType {
        TOP, BOTTOM, LEFT, RIGHT, UP, DOWN
    }

    record Side(SideType sideType, Destination up, Destination left, Destination down, Destination right) { }

    record LocalCoordinate(int x, int y) {
        public LocalCoordinate transform(Transform transform, LocalCoordinate from, int tileSize) {
            return switch (transform) {
                case FLIP_X -> new LocalCoordinate(tileSize - 1 - from.x, from.y);
                case FLIP_Y -> new LocalCoordinate(x, tileSize - 1 - from.y);
                case X_TO_Y -> new LocalCoordinate(y, x);
                case FLIP_Y_TO_FLIP_X -> new LocalCoordinate(tileSize - 1 - from.y, tileSize - 1 - from.x);
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

    static class SecondMoveLogic implements MoveLogic{

        private final CubeLayout cubeLayout;

        private Board board;

        SecondMoveLogic(CubeLayout cubeLayout) {
            this.cubeLayout = cubeLayout;
        }

        @Override
        public void init(Board board) {
            this.board = board;
        }

        @Override
        public Cursor move(Cursor from, int steps) {
            if (steps == 0) {
                return from;
            }

            var candidate = from.next();

            var tileAtCandidate = board.get(candidate.position);

            if (tileAtCandidate == EMPTY) {
                candidate = cubeLayout.when(board, from);
                tileAtCandidate = board.get(candidate.position);
            }

            return switch (tileAtCandidate) {
                case OPEN -> move(candidate, steps - 1);
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
    }

    record Scenario(Board board, Operations operations) {
        static Scenario parse(String string) {
            var segments = string.split("\\n\\n");
            return new Scenario(
                Board.parse(segments[0]),
                Operations.parse(segments[1])
            );
        }

        public int playWith(MoveLogic moveLogic) {
            var result = new Cursor(board.leftmostOpenTileOfTheTopRowOfTiles(), Orientation.RIGHT);

            moveLogic.init(board);
            for(var operation : operations.list) {
                result = operation.apply(result, moveLogic);
            }

            return (result.position.y + 1) * 1000 + (result.position.x + 1) * 4 + result.orientation.score;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> Scenario.parse(input).playWith(new FirstMoveLogic());
    }

    @Override
    public Assignment second() {
        return (run, input) -> Scenario.parse(input).playWith(new SecondMoveLogic(cubeLayout(run)));
    }

    enum Transform {
        FLIP_Y, X_TO_Y, FLIP_Y_TO_FLIP_X, FLIP_X

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
                new Destination(LEFT, Orientation.DOWN, Transform.X_TO_Y),
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
                new Destination(BOTTOM, Orientation.RIGHT, Transform.FLIP_Y_TO_FLIP_X),
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
                new Destination(DOWN, Orientation.LEFT, Transform.FLIP_Y_TO_FLIP_X),
                new Destination(BOTTOM, Orientation.LEFT, Transform.FLIP_X),
                new Destination(UP, Orientation.RIGHT, Transform.FLIP_Y_TO_FLIP_X),
                new Destination(TOP, Orientation.LEFT, Transform.FLIP_Y));

        return new CubeLayout(new Side[][] {
                new Side[] { null, null, top, null },
                new Side[] { up, left, down, null },
                new Side[] { null, null, bottom, right }
        });

    }

    public CubeLayout realLayout() {
        // 1
        var top = new Side(TOP,
                new Destination(UP, Orientation.RIGHT, Transform.X_TO_Y),
                new Destination(LEFT, Orientation.RIGHT, Transform.FLIP_Y),
                new Destination(DOWN, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(RIGHT, Orientation.RIGHT, Transform.FLIP_X));
        // 6
        var up = new Side(UP,
                new Destination(LEFT, Orientation.UP, Transform.FLIP_Y),
                new Destination(TOP, Orientation.DOWN, Transform.X_TO_Y),
                new Destination(RIGHT, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(BOTTOM, Orientation.UP, Transform.X_TO_Y));
        // 5
        var left = new Side(LEFT,
                new Destination(DOWN, Orientation.RIGHT, Transform.X_TO_Y),
                new Destination(TOP, Orientation.RIGHT, Transform.FLIP_Y),
                new Destination(UP, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(BOTTOM, Orientation.RIGHT, Transform.FLIP_X));
        // 3
        var down = new Side(DOWN,
                new Destination(TOP, Orientation.UP, Transform.FLIP_Y),
                new Destination(LEFT, Orientation.DOWN, Transform.X_TO_Y),
                new Destination(BOTTOM, Orientation.DOWN, Transform.FLIP_Y),
                new Destination(RIGHT, Orientation.UP, Transform.X_TO_Y));
        // 4
        var bottom = new Side(BOTTOM,
                new Destination(DOWN, Orientation.UP, Transform.FLIP_Y),
                new Destination(LEFT, Orientation.LEFT, Transform.FLIP_X),
                new Destination(UP, Orientation.LEFT, Transform.X_TO_Y),
                new Destination(RIGHT, Orientation.LEFT, Transform.FLIP_Y));
        // 2
        var right = new Side(RIGHT,
                new Destination(UP, Orientation.UP, Transform.FLIP_Y),
                new Destination(TOP, Orientation.LEFT, Transform.FLIP_X),
                new Destination(DOWN, Orientation.LEFT, Transform.X_TO_Y),
                new Destination(BOTTOM, Orientation.LEFT, Transform.FLIP_Y));

        return new CubeLayout(new Side[][] {
                new Side[] { null, top, right },
                new Side[] { null, down, null },
                new Side[] { left, bottom, null },
                new Side[] { up, null, null }
        });
    }
}
