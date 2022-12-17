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

        private final int height;

        BlockType(String[] shape) {
            this.shape = Arrays.stream(shape).mapToInt(string -> valueForString(" " + string.replace('@', '#') + " ")).toArray();
            this.width = shape[0].length();
            this.height = shape.length;
        }

        static BlockType first() {
            return values()[0];
        }

        BlockType next() {
            return BlockType.values()[(Arrays.asList(values()).indexOf(this) + 1) % BlockType.values().length];
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
        private final Move[] shifts;

        private long nextIndex = 0;

        private static final Move LEFT = new Move(-1, 0);
        private static final Move RIGHT = new Move(1, 0);

        Wind(Move[] shifts) {
            this.shifts = shifts;
        }

        static Wind parse(String string) {
            return new Wind(string.chars().mapToObj(c -> c == '>' ? RIGHT : LEFT).toArray(Move[]::new));
        }

        public Move next() {
            return shifts[(int)(nextIndex++ % shifts.length)];
        }
    }

    static class Cave {
        private static final int BUFFER_SIZE = 1000;
        private static final int BUFFER_SHIFT = 100;


        private final int[] buffer = new int[BUFFER_SIZE];

        private int top = 0;
        private int bottom = 0;

        private int bufferSize = 0;

        long removedToOptimize = 0L;

        private static final int WIDTH = 7;
        private static final int EMPTY_ROWS_ABOVE_STACK = 3;

        private static final int INITIAL_X = 2;

        private static final int BOTTOM = 127;

        private static final int EMPTY = 0;

        private final Wind wind;

        private final Move gravity = new Move(0, -1);

        private BlockType currentType;
        private int currentX;
        private int currentY;

        public void updateBufferSize() {
            if (top >= bottom) {
                bufferSize = top - bottom;
            } else {
                bufferSize = top + BUFFER_SIZE - bottom;
            }
        }

        public void bufferAdd(int value) {
            top = (top + 1) % BUFFER_SIZE;
            buffer[top] = value;

            updateBufferSize();
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

            currentType = blockType;
            currentY = bufferSize + blockType.height + EMPTY_ROWS_ABOVE_STACK - 1;
            currentX = INITIAL_X;
        }

        private void removeRedundantRowsAtTheBottom() {
            if (bufferSize > BUFFER_SIZE - BUFFER_SHIFT) {
                bottom = (bottom + BUFFER_SHIFT) % BUFFER_SIZE;
                removedToOptimize += BUFFER_SHIFT;

                updateBufferSize();
            }
        }

        boolean tick() {
            apply(wind.next());

            if (!apply(gravity)) {
                save();
                addBlock(currentType.next());

                return true;
            }

            return false;
        }

        boolean apply(Move move) {
            var candidateX = currentX + move.x;
            var candidateY = currentY + move.y;

            if (candidateY < 0 ||
                candidateX < 0 ||
                (candidateX + currentType.width) > WIDTH) {
                return false;
            }

            boolean possible = true;

            for (int i = 0; i < currentType.height; i++) {
                int rowIndex = candidateY - i;
                int world = valueForLine(rowIndex);
                int shapeLine = currentType.shape[i];
                shapeLine = shapeLine << candidateX;

                int masked = shapeLine & world;

                possible &= masked == 0;
            }

            if (possible) {
                currentX = candidateX;
                currentY = candidateY;

                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            var lines = new ArrayList<String>();

            for (int y = Math.max(bufferSize - 1, currentY); y >= -1; y--) {
                lines.add(stringForLine(y));
            }

            return "\n" + String.join("\n", lines);
        }

        private void save() {
            for (int y = currentType.height - 1; y >= 0; y--) {
                var rowIndex = currentY - y;

                if (bufferSize < rowIndex + 1) {
                    bufferAdd(EMPTY);
                }

                var caveValue = bufferGet(rowIndex);
                int shapeValue = currentType.shape[y];
                shapeValue = shapeValue << currentX;

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

        private String stringForLine(int y) {
            String result;
            if (y < 0) {
                result = CORNER + stringForValue(BOTTOM) + CORNER;
            } else if (y < bufferSize) {
                result = WALL + stringForValue(bufferGet(y)) + WALL;
            } else {
                result = WALL + stringForValue(EMPTY) + WALL;
            }

            int blockTop = currentY;
            int blockBottom = currentY - currentType.height + 1;

            if (y <= blockTop && y >= blockBottom) {
                int blockLineIndex = blockTop - y;

                String blockString = stringForValue(currentType.shape[blockLineIndex]);

                StringBuilder withBlock = new StringBuilder(result);

                for (int x = 0; x < currentType.width; x++) {
                    var c = blockString.charAt(x);

                    if (c == '#') {
                        withBlock.setCharAt(currentX + x + 1, CURRENT_CHAR);
                    }
                }

                result = withBlock.toString();
            }

            return result;
        }

        private int valueForLine(int y) {
            if (y < 0) {
                return BOTTOM;
            } else if (y < bufferSize) {
                return bufferGet(y);
            } else {
                return EMPTY;
            }
        }

        public long height() {
            return removedToOptimize + bufferSize;
        }

        public long heightAfter(long blockCount) {
            var blockCounter = 0L;

            var tickCounter = 0L;

            while(blockCounter != blockCount) {
//                LOG.info(this::toString);
                tickCounter++;
                blockCounter += tick() ? 1 : 0;


                if (tickCounter % 100_000_000 == 0) {
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
