package nl.q8p.aoc2022.day15;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day15 implements Day {

    static class Point {
        int x;
        int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }


        int distanceTo(Point other) {
            return Math.abs(x - other.x) + Math.abs(y - other.y);
        }

        static Point parse(String string) {
            return new Point(
                Integer.parseInt(string.substring(string.indexOf("x=") + "x=".length(), string .indexOf(','))),
                Integer.parseInt(string.substring(string.indexOf("y=") + "y=".length()))
            );
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (x != point.x) return false;
            return y == point.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
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

        static boolean hasCoverage(Point point, Map<Point, Thing> things, Collection<Sensor> sensors) {
            return !things.containsKey(point) && sensors.stream().anyMatch(s -> s.covers(point));
        }

        static boolean isUncovered(Point point, Map<Point, Thing> things, Collection<Sensor> sensors) {
            return !things.containsKey(point) && sensors.stream().noneMatch(s -> s.covers(point));
        }

        List<Point> uncoveredPointsBetween(Point from, Point until) {
            final int chunks = 100;
            final int chunkSize = (until.y - from.y) / chunks;

            return IntStream.range(0, chunks)
                    .parallel()
                    .boxed()
                    .flatMap(chunk -> uncoveredPointsBetweenForThread(new Point(from.x, from.y + chunk * chunkSize), new Point(until.x, from.y + (chunk + 1) * chunkSize)).stream())
                    .toList();
        }

        List<Point> uncoveredPointsBetweenForThread(Point from, Point until) {
            var result = new ArrayList<Point>();

            var sensorByVisibleFromY = sensors.stream()
                    .filter(s -> s.invisibleAfterY >= from.y)
                    .collect(Collectors.groupingBy(s -> Math.max(from.y, s.visibleFromY)));

            var currentX = from.x;
            var currentY = from.y;

            var relevantSensors = new ArrayList<>(sensorByVisibleFromY.get(from.y));

            var cursor = new Point(0, 0);

            while (currentX <= until.x && currentY <= until.y) {
                cursor.x = currentX;
                cursor.y = currentY;

                var covered = things.containsKey(cursor);

                var iterator = relevantSensors.iterator();
                var newCurrentX = currentX;
                while (iterator.hasNext()) {
                    var sensor = iterator.next();

                    covered |= sensor.covers(cursor);

                    if (sensor.covers(cursor)) {
                        newCurrentX = Math.max(newCurrentX, sensor.lastCoveredXAt(cursor.y) + 1);
                    }
                    if (sensor.invisibleAfterY == currentY) {
                        iterator.remove();
                    }
                }
                if (newCurrentX != currentX) {
                    currentX = newCurrentX;
                } else {
                    currentX = Integer.MAX_VALUE;
                }

                if (!covered) {
                    result.add(cursor);
                }

                if (currentX > until.x) {
                    currentX = from.x;
                    currentY++;

                    var sensorsToAdd = sensorByVisibleFromY.get(currentY);
                    if (sensorsToAdd != null) {
                        relevantSensors.addAll(sensorsToAdd);
                    }
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
                hasCoverage = hasCoverage(new Point(x, y), things, sensors);

                if (hasCoverage) {
                    totalCoverage++;
                }
                x--;
            }

            // right from min-x, excluding min-x
            x = minX + 1;
            hasCoverage = true;
            while (hasCoverage || x <= maxX) {
                hasCoverage = hasCoverage(new Point(x, y), things, sensors);

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
