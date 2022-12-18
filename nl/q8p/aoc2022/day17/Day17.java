package nl.q8p.aoc2022.day17;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        private final int[] shifts;
        private final int length;

        private int nextIndex = 0;

        private static final Move LEFT = new Move(-1, 0);
        private static final Move RIGHT = new Move(1, 0);

        Wind(int[] shifts) {
            var copies = 10;
            this.shifts = new int[shifts.length * copies];
            this.length = shifts.length;

            for (int i = 0; i < copies; i++) {
                System.arraycopy(shifts, 0, this.shifts, length * i, length);
            }
        }

        static Wind parse(String string) {
            return new Wind(string.chars().map(c -> c == '>' ? 1 : -1).toArray());
        }

        public int next() {
            var result = shifts[nextIndex % length];
            nextIndex = (nextIndex + 1) % length;

            return result;
        }

        public int index() {
            return nextIndex;
        }

        public int[] get(int number) {
            var result = new int[number];
            System.arraycopy(shifts, nextIndex, result, 0, number);

            return result;
        }
    }

    static class Cave {
        private static final int BUFFER_SIZE = 1000000;


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

        private static final BlockType[] BLOCK_TYPES = new BlockType[] {
                BlockType.DASH,
                BlockType.PLUS,
                BlockType.ANGLE,
                BlockType.PIPE,
                BlockType.BLOCK
        };

        private int currentBlockTypeIndex;
        private int currentX;
        private int currentY;


        private int nextBlockTypeIndex() {
            return (currentBlockTypeIndex + 1) % BLOCK_TYPES.length;
        }

        static int firstBlockTypeIndex() {
            return 0;
        }

        private final Map<List<Integer>, List<Integer>>[][] windIndexToBlockTypeToRows;

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

        public void bufferSet(int index, int value) {
            var bufferIndex = (bottom + index) % BUFFER_SIZE;

            buffer[bufferIndex] = value;
        }

        public Cave(Wind wind) {
            this.wind = wind;
            windIndexToBlockTypeToRows = (Map<List<Integer>, List<Integer>>[][]) new HashMap[wind.shifts.length][];

            for (int w = 0; w < windIndexToBlockTypeToRows.length; w++) {
                windIndexToBlockTypeToRows[w] = (Map<List<Integer>, List<Integer>>[]) new HashMap[BlockType.values().length];

                for (int b = 0; b < BlockType.values().length; b++) {
                    windIndexToBlockTypeToRows[w][b] = new HashMap<>();
                }
            }

            addBlock(firstBlockTypeIndex());
        }

        void addBlock(int blockTypeIndex) {
            removeRedundantRowsAtTheBottom();

            currentBlockTypeIndex = blockTypeIndex;
            var currentType = BLOCK_TYPES[currentBlockTypeIndex];
            currentY = bufferSize + currentType.height - 1;
            currentX = INITIAL_X;

            for (int i = 0; i < EMPTY_ROWS_ABOVE_STACK; i++) {
                currentX += wind.next();
                currentX = Math.max(0, currentX);
                currentX = Math.min(WIDTH - currentType.width, currentX);
            }
        }

        private void removeRedundantRowsAtTheBottom() {
            for (int i = bufferSize - 2; i >= 0; i--) {
                if ((valueForLine(i - 1) | valueForLine(i)) == BOTTOM) {
                    var newBottom = (bottom + i) % BUFFER_SIZE;

                    int removingLines;

                    if (newBottom >= bottom) {
                        removingLines = newBottom - bottom;
                    } else {
                        removingLines = newBottom + BUFFER_SIZE - bottom;
                    }

                    removedToOptimize += removingLines;
                    bottom = newBottom;
                    updateBufferSize();
                }
            }
        }

        long hit = 0;
        long total = 0;

        void tick() {
            boolean blockAdded = false;

            total++;
            List<Integer> startedWith = recordsAsList();
            var newRecords = windIndexToBlockTypeToRows[wind.index()][currentBlockTypeIndex].get(startedWith);

            if (newRecords == null) {
                do {
                    applyWind(wind.next());

                    if (!applyGravity()) {
                        save();
                        addBlock(nextBlockTypeIndex());

                        blockAdded = true;
                    }
                } while (!blockAdded);

                windIndexToBlockTypeToRows[wind.index()][currentBlockTypeIndex].put(startedWith, recordsAsList());
            } else {
                hit++;
                for (int i = 0; i < newRecords.size(); i++) {
                    buffer[i] = newRecords.get(i);
                }

                bottom = 0;
                top = newRecords.size();
                bufferSize = newRecords.size();
            }
            if (total % 1_000_000 == 0) {
                LOG.info("wind: " + hit + " " + total + " = " + ((double) hit / total * 100));
            }
        }

        List<Integer> recordsAsList() {
            var result = new ArrayList<Integer>(bufferSize);

            for (int rowIndex = 0; rowIndex < bufferSize; rowIndex++) {
                result.add(buffer[(bottom + rowIndex) % BUFFER_SIZE]);
            }

            return result;
        }

        boolean applyWind(int deltaX) {
            var candidateX = currentX + deltaX;

            var currentType = BLOCK_TYPES[currentBlockTypeIndex];

            if (candidateX < 0 || (candidateX + currentType.width) > WIDTH) {
                return false;
            }

            boolean possible = true;

            for (int i = 0; i < currentType.height; i++) {
                int rowIndex = currentY - i;

                int masked = valueForLine(rowIndex) & currentType.shape[i] << candidateX;

                possible &= masked == 0;
            }

            if (possible) {
                currentX = candidateX;

                return true;
            }

            return false;
        }

        boolean applyGravity() {
            var candidateY = currentY - 1;

            if (candidateY < 0) {
                return false;
            }

            boolean possible = true;
            var currentType = BLOCK_TYPES[currentBlockTypeIndex];

            for (int i = 0; i < currentType.height; i++) {
                int rowIndex = candidateY - i;
                int world = valueForLine(rowIndex);

                int masked = currentType.shape[i] << currentX & world;

                possible &= masked == 0;
            }

            if (possible) {
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
            var currentType = BLOCK_TYPES[currentBlockTypeIndex];
            for (int y = currentType.height - 1; y >= 0; y--) {
                var rowIndex = currentY - y;

                if (bufferSize < rowIndex + 1) {
                    bufferAdd(EMPTY);
                }

                var caveValue = buffer[(bottom + rowIndex) % BUFFER_SIZE];
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
                result = WALL + stringForValue(valueForLine(y)) + WALL;
            } else {
                result = WALL + stringForValue(EMPTY) + WALL;
            }

            var currentType = BLOCK_TYPES[currentBlockTypeIndex];

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
                return buffer[(bottom + y) % BUFFER_SIZE];
            } else {
                return EMPTY;
            }
        }

        public long height() {
            return removedToOptimize + bufferSize;
        }

        public long heightAfter(long blockCount) {
            var blockCounter = 0L;

            while(blockCounter != blockCount) {
//                LOG.info(toString());
                tick();
                blockCounter++;

                if (blockCounter % 100_000_000 == 0) {
                    LOG.info("" + blockCounter + " / " + blockCount + " (" + ((double)blockCounter / blockCount * 100L) + "%)");
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
