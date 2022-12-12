package nl.q8p.aoc2022.day12;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparingInt;

public class Day12 implements Day {

    static class HeightMap {
        private final int[][] data;

        HeightMap(int[][] data) {
            this.data = data;
        }

        int width() {
            return data[0].length;
        }

        int height() {
            return data.length;
        }

        int elevation(Position position) {
            return data[position.y][position.x];
        }

        boolean exists(Position from, Position to) {
            return to.x >= 0 && to.x < width() && to.y >= 0 && to.y < height() && ((elevation(to) - elevation(from)) <= 1);
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
        Position destination() {
            return path.get(path.size() - 1);
        }
    }

    record Me(HeightMap map, Position current, Position target) {

        @Override
        public String toString() {
            var buffer = new StringBuilder();

            buffer.append("map: \n");
            for (var y = 0; y < map.height(); y++) {
                for (var x = 0; x < map.width(); x++) {
                    var position = new Position(x, y);

                    char representation;
                    if (current.equals(position)) {
                        representation = 'S';
                    } else if (target.equals(position)) {
                        representation = 'E';
                    } else {
                        representation = (char)('a' + map.elevation(position));
                    }

                    buffer.append(representation);
                }
                buffer.append("\n");
            }

            return buffer.toString();
        }

        Optional<Route> route() {
            Map<Position, Route> routeToDestination = new HashMap<>();

            routeToDestination.put(current, new Route(List.of(current)));

            boolean foundNewPaths;

            do {
                foundNewPaths = false;

                for (var route : new ArrayList<>(routeToDestination.values())) {
                    for (var move : route.destination().moves().stream().filter(move -> map.exists(route.destination(), move)).toList()) {
                        var existingPath = routeToDestination.get(move);

                        if (existingPath == null || existingPath.path.size() > (route.path.size() + 1)) {
                            foundNewPaths = true;

                            var newPath = new ArrayList<>(route.path);
                            newPath.add(move);

                            routeToDestination.put(move, new Route(newPath));
                        }
                    }
                }
            } while (foundNewPaths);

            return routeToDestination.values().stream()
                    .filter(route -> route.destination().equals(target))
                    .min(comparingInt(route -> route.path.size()));
        }
    }


    @Override
    public Assignment first() {
        return input -> {
            var lines = input.split("\\n");

            var heightMap = new HeightMap(Arrays.stream(lines)
                    .map(l -> l.replace('S', 'a').replace('E', 'z').chars().map(c -> c - 'a').toArray())
                    .toArray(int[][]::new));

            Position current = null;
            Position target = null;

            for (int y = 0; y < lines.length; y++) {
                for (int x = 0; x < lines[y].length(); x++) {
                    switch (lines[y].charAt(x)) {
                        case 'S' -> current = new Position(x, y);
                        case 'E' -> target = new Position(x, y);
                    }
                }
            }

            var me = new Me(heightMap, current, target);

            return me.route().orElseThrow().path.size() - 1;
        };
    }

    @Override
    public Assignment second() {
        return input -> {
            var lines = input.split("\\n");

            var heightMap = new HeightMap(Arrays.stream(lines)
                    .map(l -> l.replace('S', 'a').replace('E', 'z').chars().map(c -> c - 'a').toArray())
                    .toArray(int[][]::new));

            Position target = null;

            List<Position> low = new ArrayList<>();

            for (int y = 0; y < lines.length; y++) {
                for (int x = 0; x < lines[y].length(); x++) {
                    switch (lines[y].charAt(x)) {
                        case 'S', 'a' -> low.add(new Position(x, y));
                        case 'E' -> target = new Position(x, y);
                    }
                }
            }

            Position finalTarget = target;
            return low.stream()
                    .map(r -> new Me(heightMap, r, finalTarget).route())
                    .filter(Optional::isPresent)
                    .mapToInt(r -> r.get().path.size())
                    .min()
                    .orElseThrow() - 1;
        };
    }


}
