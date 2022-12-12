package nl.q8p.aoc2022.day12;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;

public class Day12 implements Day {

    static class HeightMap {
        private final char[][] data;

        HeightMap(char[][] data) {
            this.data = data;
        }

        int width() {
            return data[0].length;
        }

        int height() {
            return data.length;
        }

        int value(Position position) {
            return data[position.y][position.x];
        }

        int elevation(Position position) {
            return switch (value(position)) {
                case 'S' -> 0;
                case 'E' -> 26;
                default -> value(position) - 'a';
            };
        }

        Stream<Position> positions() {
            return IntStream.range(0, height())
                    .boxed()
                    .flatMap(y -> IntStream.range(0, width()).mapToObj(x -> new Position(x, y)));
        }

        List<Position> positionsOnGroundLevel() {
            return positions().filter(p -> elevation(p) == 0).toList();
        }

        Position start() {
            return getPositionsWithValue('S').findFirst().orElseThrow();
        }

        Position end() {
            return getPositionsWithValue('E').findFirst().orElseThrow();
        }

        private Stream<Position> getPositionsWithValue(char value) {
            return positions().filter(p -> value(p) == value);
        }

        boolean allowed(Position from, Position to) {
            return to.x >= 0 && to.x < width() && to.y >= 0 && to.y < height() && ((elevation(to) - elevation(from)) <= 1);
        }

        Optional<Route> route(List<Position> startingPoints) {
            var routeToDestination = new HashMap<>(startingPoints.stream().collect(Collectors.toMap(s -> s, s -> new Route(List.of(s)))));

            boolean foundNewPaths;

            do {
                foundNewPaths = false;

                for (var route : new ArrayList<>(routeToDestination.values())) {
                    for (var move : route.end().moves().stream().filter(move -> allowed(route.end(), move)).toList()) {
                        var existingPath = routeToDestination.get(move);

                        if (existingPath == null || existingPath.path.size() > (route.path.size() + 1)) {
                            foundNewPaths = true;

                            routeToDestination.put(move, new Route(Stream.concat(route.path.stream(), Stream.of(move)).toList()));
                        }
                    }
                }
            } while (foundNewPaths);

            return routeToDestination.values().stream()
                    .filter(route -> route.end().equals(end()))
                    .min(comparingInt(route -> route.path.size()));
        }
    }

    record Position(int x, int y) {
        List<Position> moves() {
            return List.of(
                    new Position(x - 1, y),
                    new Position(x + 1, y),
                    new Position(x, y - 1),
                    new Position(x, y + 1)
            );
        }
    }

    record Route(List<Position> path) {
        Position end() {
            return path.get(path.size() - 1);
        }
    }

    @Override
    public Assignment first() {
        return input -> {
            var heightMap = getHeightMap(input);

            var route = heightMap.route(List.of(heightMap.start())).orElseThrow();

            return route.path.size() - 1;
        };
    }

    private static HeightMap getHeightMap(String input) {
        return new HeightMap(Arrays.stream(input.split("\\n")).map(String::toCharArray).toArray(char[][]::new));
    }

    @Override
    public Assignment second() {
        return input -> {
            var heightMap = getHeightMap(input);

            var route = heightMap.route(heightMap.positionsOnGroundLevel()).orElseThrow();

            return route.path.size() - 1;
        };
    }


}
