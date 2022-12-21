package nl.q8p.aoc2022.day15;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day15 implements Day {

    record Point(int x, int y) {
        int distanceTo(Point other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y);
        }

        static Point parse(String string) {
            return new Point(
                Integer.parseInt(string.substring(string.indexOf("x=") + "x=".length(), string .indexOf(','))),
                Integer.parseInt(string.substring(string.indexOf("y=") + "y=".length()))
            );
        }
    }

    static class Sensor {
        private final Point position;
        private final Point beacon;
        private final int distanceToBeacon;
        private final int visibleFromY;
        private final int invisibleAfterY;

        public Sensor(Point position, Point beacon) {
            this.position = position;
            this.beacon = beacon;
            this.distanceToBeacon = position.distanceTo(beacon);
            this.visibleFromY = position.y - distanceToBeacon;
            this.invisibleAfterY = position.y + distanceToBeacon;
        }

        boolean covers(Point point) {
            return distanceToBeacon >= position.distanceTo(point);
        }

        static Sensor parse(String string) {
            return new Sensor(
                Point.parse(string.substring(string.indexOf("Sensor at ") + "Sensor at ".length(), string.indexOf(':'))),
                Point.parse(string.substring(string.indexOf("closest beacon is at ") + "closest beacon is at ".length()))
            );
        }

        public int lastCoveredXAt(int y) {
            return position.x + distanceToBeacon - Math.abs(position.y - y);
        }
    }

    enum Thing {
        SENSOR, BEACON
    }

    static class World {

        private final List<Sensor> sensors;
        private final Map<Point, Thing> things = new HashMap<>();

        World(List<Sensor> sensors) {
            this.sensors = sensors;

            sensors.forEach(s -> {
                things.put(s.position, Thing.SENSOR);
                things.put(s.beacon, Thing.BEACON);
            });
        }

        static World parse(String string) {
            return new World(Arrays.stream(string.split("\n")).map(Sensor::parse).toList());
        }

        boolean hasCoverage(Point point) {
            return !things.containsKey(point) && sensors.stream().anyMatch(s -> s.covers(point));
        }

        boolean isUncovered(Point point) {
            return !things.containsKey(point) && sensors.stream().noneMatch(s -> s.covers(point));
        }

        List<Point> uncoveredPointsBetween(Point from, Point until) {
            final int chunks = 1000;
            final int chunkSize = (until.y - from.y) / chunks;

            return IntStream.range(0, chunks)
                    .parallel()
                    .boxed()
                    .flatMap(chunk -> uncoveredPointsBetweenForThread(new Point(from.x, from.y + chunk * chunkSize), new Point(until.x, from.y + (chunk + 1) * chunkSize)).stream())
                    .toList();
        }

        List<Point> uncoveredPointsBetweenForThread(Point from, Point until) {
            var result = new ArrayList<Point>();

            var sensorByVisibleFromY = sensors.stream().collect(Collectors.groupingBy(s -> Math.max(from.y, s.visibleFromY)));
            var sensorByInvisibleFromY = sensors.stream().collect(Collectors.groupingBy(s -> s.invisibleAfterY));

            var currentX = from.x;
            var currentY = from.y;

            var relevantSensors = new HashSet<>(sensorByVisibleFromY.get(from.y));

            while (currentX <= until.x && currentY <= until.y) {
                var current = new Point(currentX, currentY);

                if (isUncovered(current)) {
                    result.add(current);
                }

                currentX = relevantSensors
                        .stream()
                        .filter(s -> s.covers(current))
                        .mapToInt(s -> s.lastCoveredXAt(current.y) + 1)
                        .max()
                        .orElse(Integer.MAX_VALUE);

                if (currentX > until.x) {
                    currentX = from.x;
                    currentY++;

                    relevantSensors.removeAll(sensorByInvisibleFromY.getOrDefault(currentY, Collections.emptyList()));
                    relevantSensors.addAll(sensorByVisibleFromY.getOrDefault(currentY, Collections.emptyList()));
                }
            }

            return result;
        }

        int coveredLinesAtRow(int y) {
            var minX = sensors.stream().mapToInt(s -> Math.min(s.beacon.x, s.position.x)).min().orElseThrow();
            var maxX = sensors.stream().mapToInt(s -> Math.max(s.beacon.x, s.position.x)).max().orElseThrow();

            var totalCoverage = 0;

            // left from min-x, including min-x
            var x = minX;
            var hasCoverage = true;
            while (hasCoverage) {
                hasCoverage = hasCoverage(new Point(x, y));

                if (hasCoverage) {
                    totalCoverage++;
                }
                x--;
            }

            // right from min-x, excluding min-x
            x = minX + 1;
            hasCoverage = true;
            while (hasCoverage || x <= maxX) {
                hasCoverage = hasCoverage(new Point(x, y));

                if (hasCoverage) {
                    totalCoverage++;
                }
                x++;
            }

            return totalCoverage;
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> World.parse(input).coveredLinesAtRow(switch (run) { case EXAMPLE -> 10; case REAL -> 2000000; });
    }

    @Override
    public Assignment second() {
        return (run, input) -> World.parse(input).uncoveredPointsBetween(new Point(0, 0), new Point(untilCoordinateOfSecondAssignment(run), untilCoordinateOfSecondAssignment(run)))
                    .stream().mapToLong(p -> (4000000L * p.x) + p.y)
                    .findFirst()
                    .orElse(-1);
    }

    private int untilCoordinateOfSecondAssignment(Assignment.Run run) {
        return switch (run) {
            case EXAMPLE -> 20;
            case REAL -> 4000000;
        };
    }
}
