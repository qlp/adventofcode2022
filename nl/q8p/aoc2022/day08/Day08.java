package nl.q8p.aoc2022.day08;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Day08 implements Day {

    record Coordinate(int column, int row) { }

    @Override
    public Assignment first() {
        return input -> {
            var heightMap = Arrays.stream(input.split("\\n")).map(line -> line.chars().map(c -> c - '0').toArray()).toArray(int[][]::new);

            return IntStream.range(0, heightMap.length).boxed().flatMap(y ->
                 IntStream.range(0, heightMap[0].length).boxed().filter(x -> {
                     if (x == 0 || x + 1 == heightMap[0].length || y == 0 || y + 1 == heightMap.length) {
                         return true;
                     }

                     var visibleFromTop = IntStream.range(0, y).allMatch(i -> heightMap[y][x] > heightMap[i][x]);
                     var visibleFromLeft = IntStream.range(0, x).allMatch(i -> heightMap[y][x] > heightMap[y][i]);

                     var visibleFromBottom = IntStream.range(y + 1, heightMap.length).allMatch(i -> heightMap[y][x] > heightMap[i][x]);
                     var visibleFromRight = IntStream.range(x + 1, heightMap[0].length).allMatch(i -> heightMap[y][x] > heightMap[y][i]);

                     return visibleFromTop || visibleFromLeft || visibleFromBottom || visibleFromRight;
                 }).map(x -> new Coordinate(x, y))
            )
            .distinct()
            .count();
        };
    }

    public int score(int[] trees) {
        var result = 0;

        var mine = trees[0];

        for (int i = 1; i < trees.length; i++) {
            result++;

            if (trees[i] >= mine) {
                break;
            }
        }

        return result;
    }

    @Override
    public Assignment second() {
        return input -> {
            var heightMap = Arrays.stream(input.split("\\n")).map(line -> line.chars().map(c -> c - '0').toArray()).toArray(int[][]::new);

            return IntStream.range(0, heightMap.length).flatMap(y ->
                    IntStream.range(0, heightMap[0].length).map(x -> {
                        var scoreFromTop = score(IntStream.rangeClosed(0, y).map(i -> heightMap[y - i][x]).toArray());
                        var scoreFromLeft = score(IntStream.rangeClosed(0, x).map(i -> heightMap[y][x - i]).toArray());

                        var scoreFromBottom = score(IntStream.range(y, heightMap.length).map(i -> heightMap[i][x]).toArray());
                        var scoreFromRight = score(IntStream.range(x, heightMap[0].length).map(i -> heightMap[y][i]).toArray());

                        return scoreFromTop * scoreFromLeft * scoreFromRight * scoreFromBottom;
                    })
            )
            .max()
            .orElseThrow();
        };
    }
}
