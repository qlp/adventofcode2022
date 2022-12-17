package nl.q8p.aoc2022.day17;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.IntStream;

public class Day17 implements Day {

    private static final Logger LOG = Logger.getLogger(Day17.class.getName());

    enum BlockType {
        DASH(new String[] { "@@@@" }),
        PLUS(new String[] { " @ ", "@@@", " @" }),
        ANGLE(new String[] { "  @", "  @", "@@@" }),
        PIPE(new String[] { "@", "@", "@", "@" }),
        BLOCK(new String[] { "@@", "@@" });

        private final String[] shape;

        int height() {
            return shape.length;
        }

        BlockType(String[] shape) {
            this.shape = shape;
        }

        static BlockType first() {
            return values()[0];
        }

        BlockType next() {
            return BlockType.values()[(Arrays.asList(values()).indexOf(this) + 1) % BlockType.values().length];
        }
    }

    record Position(int x, int y) {
        Position(int y) {
            this(3, y);
        }

        public Position with(Move move) {
            return new Position(x + move.x, y + move.y);
        }
    }

    record Block(BlockType type, Position position) {
        public Block with(Move move) {
            return new Block(type, position.with(move));
        }
    }

    record Move(int x, int y) { }

    enum Shift {
        LEFT(new Move(-1, 0)),
        RIGHT(new Move(1, 0));

        final Move move;

        Shift(Move move) {
            this.move = move;
        }
    }

    static class Wind {
        private final List<Shift> shifts;

        private int nextIndex = 0;

        private Block block;

        Wind(List<Shift> shifts) {
            this.shifts = shifts;
        }

        static Wind parse(String string) {
            return new Wind(string.chars().mapToObj(c -> ((char)c) == '>' ? Shift.RIGHT : Shift.LEFT).toList());
        }

        public Shift next() {
            return shifts.get(nextIndex++ % shifts.size());
        }
    }

    static class Cave {
        LinkedList<String> rows = new LinkedList<>();

        int width = 7;
        int emptyRowsAboveStack = 3;

        static final String AIR = ".";
        static final String VOID = ".";
        static final String WALL = "|";
        static final String BOTTOM = "-";
        static final String BLOCK = "#";
        static final String CORNER = "+";

        static final char CURRENT_CHAR = '@';

        static final String CURRENT = "" + CURRENT_CHAR;

        static String UNMOVABLE = WALL + BOTTOM + BLOCK + CORNER;

        String emptyRow = AIR.repeat(width);
        String voidRow = VOID.repeat(width);

        String bottomRow = BOTTOM.repeat(width);
        private final Wind wind;

        private final Move gravity = new Move(0, -1);

        private Block current;

        public Cave(Wind wind) {
            this.wind = wind;

            addBlock(BlockType.first());
        }

        void addBlock(BlockType blockType) {
            removeEmptyRowsAtTheTop();

            this.current = new Block(blockType, new Position(rows.size() + blockType.height() + emptyRowsAboveStack - 1));
        }

        void removeEmptyRowsAtTheTop() {
            for (int rowNumber = rows.size(); rowNumber >= 0; rowNumber--) {
                if(rowNumber == 0 || !rows.get(rowNumber - 1).equals(emptyRow)) {
                    if (rowNumber > rows.size()) {
                        IntStream.range(0, rowNumber - rows.size()).forEach(i -> rows.add(emptyRow));
                    } else {
                        IntStream.range(0, rows.size() - rowNumber).forEach(i -> rows.removeLast());
                    }

                    break;
                }
            }
        }

        boolean tick() {
            apply(wind.next().move);

            if (!apply(gravity)) {
                save(current);
                addBlock(current.type.next());

                return true;
            }

            return false;
        }

        boolean apply(Move move) {
            var candidate = current.with(move);

            String[] currentView = stringsAroundBlock(current, current);
            var before = countImmutables(currentView);
            String[] afterView = stringsAroundBlock(current, candidate);
            var after = countImmutables(afterView);

            if (before == after) {
                current = candidate;

                return true;
            }

            return false;
        }

        private int countImmutables(String[] stringsAroundBlock) {
            return Arrays.stream(stringsAroundBlock).mapToInt(line -> (int)line.chars().filter(c -> UNMOVABLE.indexOf(c) != -1).count()).sum();
        }

        @Override
        public String toString() {
            var lines = new ArrayList<String>();

            for (int y = Math.max(rows.size() - 1, current.position.y); y >= -1; y--) {
                lines.add(stringForLine(y, current));
            }

            return "\n" + String.join("\n", lines);
        }

        private String[] stringsAroundBlock(Block position, Block representation) {
            var result = new String[position.type.height() + 1];

            for (int y = 0; y < position.type.height() + 1; y++) {
                result[y] = stringForLine(position.position.y - y, representation);
            }

            return result;
        }

        private void save(Block block) {
            for (int y = block.type.height() - 1; y >= 0; y--) {
                var rowIndex = block.position.y - y;

                if (rows.size() < rowIndex + 1) {
                    rows.add(emptyRow);
                }

                var update = stringForLine(rowIndex, block).replace(CURRENT, BLOCK).replace(VOID, AIR);
                rows.set(rowIndex, update.substring(1, update.length() - 1));
            }
        }

        private String stringForLine(int y, Block block) {
            String result;
            if (y < 0) {
                result = CORNER + bottomRow + CORNER;
            } else if (y < rows.size()) {
                result = WALL + rows.get(y) + WALL;
            } else {
                result = WALL + voidRow + WALL;
            }

            int blockTop = block.position.y;
            int blockBottom = block.position.y - block.type.height() + 1;

            if (y <= blockTop && y >= blockBottom) {
                int blockLineIndex = blockTop - y;

                String blockString = block.type.shape[blockLineIndex];

                StringBuilder withBlock = new StringBuilder(result);

                for (int x = 0; x < blockString.length(); x++) {
                    var c = blockString.charAt(x);

                    if (c == CURRENT_CHAR) {
                        withBlock.setCharAt(block.position.x + x, CURRENT_CHAR);
                    }
                }

                result = withBlock.toString();
            }

            return result;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var cave = new Cave(Wind.parse(input));

            var blockCounter = 0;

            while(blockCounter != 2022) {
                blockCounter += cave.tick() ? 1 : 0;
//                LOG.info(cave::toString);
            }

            return cave.rows.size();
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
