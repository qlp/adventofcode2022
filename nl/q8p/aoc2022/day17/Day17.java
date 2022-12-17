package nl.q8p.aoc2022.day17;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day17 implements Day {

    private static final Logger LOG = Logger.getLogger(Day17.class.getName());

    private static final String WALL = "|";
    private static final String BOTTOM = "-";
    private static final String BLOCK = "#";
    private static final String CORNER = "+";

    private static final char CURRENT_CHAR = '@';

    private static final String UNMOVABLE = WALL + BOTTOM + BLOCK + CORNER;

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
        PLUS(new String[] { " @ ", "@@@", " @ " }),
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
            this(2, y);
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

        private long nextIndex = 0;

        Wind(List<Shift> shifts) {
            this.shifts = shifts;
        }

        static Wind parse(String string) {
            return new Wind(string.chars().mapToObj(c -> ((char)c) == '>' ? Shift.RIGHT : Shift.LEFT).toList());
        }

        public Shift next() {
            return shifts.get((int)(nextIndex++ % shifts.size()));
        }
    }

    static class Cave {
        private static final int BUFFER_SIZE = 1000;
        private static final int BUFFER_SHIFT = 100;

        private int[] buffer = new int[BUFFER_SIZE];

        private int top = 0;
        private int bottom = 0;

        long removedToOptimize = 0L;

        private static final int WIDTH = 7;
        private static final int EMPTY_ROWS_ABOVE_STACK = 3;

        private static final int BOTTOM = 127;

        private static final int EMPTY = 0;

        private final Wind wind;

        private final Move gravity = new Move(0, -1);

        private Block current;

        public int bufferSize() {
            if (top >= bottom) {
                return top - bottom;
            }

            return top + BUFFER_SIZE - bottom;
        }

        public void bufferAdd(int value) {
            top = (top + 1) % BUFFER_SIZE;
            buffer[top] = value;
        }

        public int bufferGet(int index) {
            var bufferIndex = (bottom + index) % BUFFER_SIZE;

            return buffer[bufferIndex];
        }

        public void bufferSet(int index, int value) {
            var bufferIndex = (bottom + index) % BUFFER_SIZE;

            buffer[bufferIndex] = value;
        }

        public Cave(Wind wind) {
            this.wind = wind;

            addBlock(BlockType.first());
        }

        void addBlock(BlockType blockType) {
            removeRedundantRowsAtTheBottom();

            this.current = new Block(blockType, new Position(bufferSize() + blockType.height() + EMPTY_ROWS_ABOVE_STACK - 1));
        }

        private void removeRedundantRowsAtTheBottom() {
            if (bufferSize() > BUFFER_SIZE - BUFFER_SHIFT) {
                bottom = (bottom + BUFFER_SHIFT) % BUFFER_SIZE;
                removedToOptimize += BUFFER_SHIFT;
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

            if (candidate.position.y < 0 ||
                candidate.position.x < 0 ||
                (candidate.position.x + candidate.type.width) > WIDTH) {
                return false;
            }

            boolean possible = true;

            for (int i = 0; i < candidate.type.height(); i++) {
                int rowIndex = candidate.position.y - i;
                int world = valueForLine(rowIndex);
                int shapeLine = candidate.type.shape[i];
                shapeLine = shapeLine << candidate.position.x;

                int masked = shapeLine & world;

                possible &= masked == 0;
            }

            if (possible) {
                current = candidate;

                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            var lines = new ArrayList<String>();

            for (int y = Math.max(bufferSize() - 1, current.position.y); y >= -1; y--) {
                lines.add(stringForLine(y, current));
            }

            return "\n" + String.join("\n", lines);
        }

        private void save(Block block) {
            for (int y = block.type.height() - 1; y >= 0; y--) {
                var rowIndex = block.position.y - y;

                if (bufferSize() < rowIndex + 1) {
                    bufferAdd(EMPTY);
                }

                var caveValue = bufferGet(rowIndex);
                int shapeValue = block.type.shape[y];
                shapeValue = shapeValue << block.position.x;

                var update = caveValue | shapeValue;
                bufferSet(rowIndex, update);
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
                result = CORNER + stringForValue(BOTTOM) + CORNER;
            } else if (y < bufferSize()) {
                result = WALL + stringForValue(bufferGet(y)) + WALL;
            } else {
                result = WALL + stringForValue(EMPTY) + WALL;
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
                        withBlock.setCharAt(block.position.x + x + 1, CURRENT_CHAR);
                    }
                }

                result = withBlock.toString();
            }

            return result;
        }

        private int valueForLine(int y) {
            if (y < 0) {
                return BOTTOM;
            } else if (y < bufferSize()) {
                return bufferGet(y);
            } else {
                return EMPTY;
            }
        }

        public long height() {
            return removedToOptimize + bufferSize();
        }

        public long heightAfter(long blockCount) {
            var blockCounter = 0L;

            var tickCounter = 0L;

            while(blockCounter != blockCount) {
//                LOG.info(this::toString);
                tickCounter++;
                blockCounter += tick() ? 1 : 0;


                if (tickCounter % 10_000_000 == 0) {
                    LOG.info("" + tickCounter + ": " + blockCounter + " / " + blockCount + " (" + ((double)blockCounter / blockCount * 100L) + "%)");
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
        return (run, input) -> new Cave(Wind.parse(input)).heightAfter(1_000_000_000_000L);
    }
}
