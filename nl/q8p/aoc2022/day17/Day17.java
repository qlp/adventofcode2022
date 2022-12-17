package nl.q8p.aoc2022.day17;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day17 implements Day {

    private static final Logger LOG = Logger.getLogger(Day17.class.getName());


    static final String AIR = ".";
    static final String VOID = ".";
    static final String WALL = "|";
    static final String BOTTOM = "-";
    static final String BLOCK = "#";
    static final String CORNER = "+";

    static final char CURRENT_CHAR = '@';

    static final String CURRENT = "" + CURRENT_CHAR;

    static String UNMOVABLE = WALL + BOTTOM + BLOCK + CORNER;

    private static int valueForString(String update) {
        int result = 0;

        String value = update.substring(1, update.length() - 1);

        for (int i = 0; i < value.length(); i++) {
            result |= UNMOVABLE.indexOf(value.charAt(i)) != -1 ? 1 << i : 0;
        }

        return result;
    }


    enum BlockType {
        DASH(new String[] { "@@@@" }),
        PLUS(new String[] { " @ ", "@@@", " @" }),
        ANGLE(new String[] { "  @", "  @", "@@@" }),
        PIPE(new String[] { "@", "@", "@", "@" }),
        BLOCK(new String[] { "@@", "@@" });

        private final int[] shape;

        private final int width;

        int height() {
            return shape.length;
        }

        BlockType(String[] shape) {
            this.shape = Arrays.stream(shape).mapToInt(string -> valueForString(" " + string.replace('@', '#') + " ")).toArray();
            this.width = shape[0].length();
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
        LinkedList<Integer> rows = new LinkedList<>();

        long removedToOptimize = 0L;

        int width = 7;
        int emptyRowsAboveStack = 3;

        private final int bottom = 127;

        private final int empty = 0;

        String emptyRow = AIR.repeat(width);
        String voidRow = VOID.repeat(width);

        String bottomRow = BOTTOM.repeat(width);
        private final Wind wind;

        private final Move gravity = new Move(0, -1);

        private Block current;

        private Map<String, Integer> unmovableCalculations = new HashMap<>();

        public Cave(Wind wind) {
            this.wind = wind;

            addBlock(BlockType.first());
        }

        void addBlock(BlockType blockType) {
            removeEmptyRowsAtTheTop();
            removeRedundantRowsAtTheBottom();

            this.current = new Block(blockType, new Position(rows.size() + blockType.height() + emptyRowsAboveStack - 1));
        }

        private void removeRedundantRowsAtTheBottom() {
            if (rows.size() > 500) {
                rows = new LinkedList<>(rows.subList(100, rows.size()));
                removedToOptimize += 100;
            }
        }

        private void removeEmptyRowsAtTheTop() {
            for (int rowNumber = rows.size(); rowNumber >= 0; rowNumber--) {
                if(rowNumber == 0 || rows.get(rowNumber - 1) != 0) {
                    if (rowNumber > rows.size()) {
                        IntStream.range(0, rowNumber - rows.size()).forEach(i -> rows.add(empty));
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
            var sum = 0;

            for (int i = 0; i < stringsAroundBlock.length; i++) {
                sum += unmovableCalculations.computeIfAbsent(stringsAroundBlock[i], s -> (int)s.chars().filter(c -> UNMOVABLE.indexOf(c) != -1).count());
            }

            return sum;
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

        private int[] valuesAroundBlock(Block position, Block representation) {
            var result = new int[position.type.height() + 1];

            for (int y = 0; y < position.type.height() + 1; y++) {
                result[y] = valueForLine(position.position.y - y, representation);
            }

            return result;
        }

        private void save(Block block) {
            for (int y = block.type.height() - 1; y >= 0; y--) {
                var rowIndex = block.position.y - y;

                if (rows.size() < rowIndex + 1) {
                    rows.add(empty);
                }

                var update = stringForLine(rowIndex, block).replace(CURRENT, BLOCK).replace(VOID, AIR);
                rows.set(rowIndex, valueForString(update));
            }
        }

        private String stringForValue(int value) {
            return String.valueOf(new char[] {
                    (value & 1) == 1 ? '#' : ' ',
                    (value & 2) == 2 ? '#' : ' ',
                    (value & 4) == 4 ? '#' : ' ',
                    (value & 8) == 8 ? '#' : ' ',
                    (value & 16) == 16 ? '#' : ' ',
                    (value & 32) == 32 ? '#' : ' ',
                    (value & 64) == 64 ? '#' : ' '
            });
        }

        private String stringForLine(int y, Block block) {
            String result;
            if (y < 0) {
                result = CORNER + stringForValue(bottom) + CORNER;
            } else if (y < rows.size()) {
                result = WALL + stringForValue(rows.get(y)) + WALL;
            } else {
                result = WALL + stringForValue(empty) + WALL;
            }

            int blockTop = block.position.y;
            int blockBottom = block.position.y - block.type.height() + 1;

            if (y <= blockTop && y >= blockBottom) {
                int blockLineIndex = blockTop - y;

                String blockString = stringForValue(block.type.shape[blockLineIndex]);

                StringBuilder withBlock = new StringBuilder(result);

                for (int x = 0; x < block.type.width; x++) {
                    var c = blockString.charAt(x);

                    if (c == '#') {
                        withBlock.setCharAt(block.position.x + x, CURRENT_CHAR);
                    }
                }

                result = withBlock.toString();
            }

            return result;
        }

        private int valueForLine(int y, Block block) {
            return valueForString(stringForLine(y, block)); // <<< TODO: optimize
        }

        public long height() {
            return removedToOptimize + rows.size();
        }

        public long heightAfter(long blockCount) {
            var blockCounter = 0L;

            var tickCounter = 0L;

            while(blockCounter != blockCount) {
//                LOG.info(this::toString);
                tickCounter++;
                blockCounter += tick() ? 1 : 0;


                if (tickCounter % 1_000_000 == 0) {
                    LOG.info("" + tickCounter + ": " + blockCounter + " / " + blockCount + " (" + (blockCounter / blockCount * 100L) + "%)");
                }
            }

            return height();
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> new Cave(Wind.parse(input)).heightAfter(2022);
    }

    @Override
    public Assignment second() {
        return (run, input) -> new Cave(Wind.parse(input)).heightAfter(1000000000000L);
    }
}
