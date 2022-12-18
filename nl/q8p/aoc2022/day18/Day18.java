package nl.q8p.aoc2022.day18;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day18 implements Day {

    private static final Logger LOG = Logger.getLogger(Day18.class.getName());

    private static final List<List<Integer>> NEIGHBOURS = List.of(
            List.of(1, 0, 0),
            List.of(-1, 0, 0),
            List.of(0, 1, 0),
            List.of(0, -1, 0),
            List.of(0, 0, 1),
            List.of(0, 0, -1)
    );

    private static final int[][] NEIGHBOURS_INT = new int[][] {
            new int[]{1, 0, 0},
            new int[]{-1, 0, 0},
            new int[]{0, 1, 0},
            new int[]{0, -1, 0},
            new int[]{0, 0, 1},
            new int[]{0, 0, -1}
    };

    private static List<Integer> add(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < left.size(); i++) {
            result.add(left.get(i) + right.get(i));
        }

        return result;
    }

    private static int[] add(int[] left, int[] right) {
        var result = new int[left.length];

        for (int i = 0; i < left.length; i++) {
            result[i] = left[i] + right[i];
        }

        return result;
    }

    enum Type {
        LAVA, WATER
    }

    @Override
    public Assignment first() {
        return (run, input) -> count(parse(input), null);
    }

    @Override
    public Assignment second() {
        return (run, input) -> count(flood(parse(input)), Type.WATER);
    }

    record Point(int x, int y, int z) {
        List<Point> neighbours() {
            return Arrays.stream(NEIGHBOURS_INT).map(n -> new Point(x + n[0], y + n[1], z + n[2])).toList();
        }
    }

    private Type[][][] flood(Type[][][] world) {
        World worldObject = new World(world);
        flood(worldObject, Set.of(new Point(0, 0, 0)));

        return worldObject.data();
    }

    private void flood(World world, Set<Point> points) {
        Set<Point> processing = new HashSet<>(points);

        while (!processing.isEmpty()) {
            var empty = processing.stream().filter(p -> world.get(p) == null).collect(Collectors.toSet());

            empty.forEach(p -> world.set(p, Type.WATER));

            processing = empty.stream()
                    .flatMap(e -> e.neighbours().stream())
                    .distinct()
                    .filter(world::contains)
                    .filter(n -> world.get(n) == null)
                    .collect(Collectors.toSet());
        }
    }

    record World(Type[][][] world) {
        Type get(Point point) {
            return world[point.x][point.y][point.z];
        }

        void set(Point point, Type type) {
            world[point.x][point.y][point.z] = type;
        }

        Type[][][] data() {
            return world;
        }

        public boolean contains(Point n) {
            return
                n.x >= 0 && n.x < world.length &&
                n.y >= 0 && n.y < world.length &&
                n.z >= 0 && n.z < world.length;
        }
    }

    private static long count(Type[][][] world, Type type) {
        return IntStream.range(1, world.length - 1)
                .mapToLong(x -> IntStream.range(1, world.length - 1)
                        .mapToLong(y -> IntStream.range(1, world.length - 1)
                                .filter(z -> world[x][y][z] == Type.LAVA)
                                .mapToLong(z -> Arrays.stream(NEIGHBOURS_INT)
                                        .map(n -> add(new int[]{x, y, z}, n))
                                        .filter(n -> world[n[0]][n[1]][n[2]] == type)
                                        .count()
                                ).sum()
                        ).sum()
                ).sum();
    }

    Type[][][] parse(String input) {
        var lava = Arrays.stream(input.split("\n")).map(l -> Arrays.stream(l.split(",")).mapToInt(Integer::parseInt).toArray()).toList();

        var max = lava.stream().flatMap(l -> Arrays.stream(l).boxed()).mapToInt(l -> l).max().orElseThrow();

        var size = max + 3; // 2 for the edge, 1 for the 0 based coordinates

        var world = new Type[size][][];
        for (int x = 0; x < size; x++) {
            world[x] = new Type[size][];
            for (int y = 0; y < size; y++) {
                world[x][y] = new Type[size];
            }
        }

        lava.forEach(l -> world[l[0] + 1][l[1] + 1][l[2] + 1] = Type.LAVA);

        return world;
    }
}
