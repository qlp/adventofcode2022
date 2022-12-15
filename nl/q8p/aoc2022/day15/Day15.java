package nl.q8p.aoc2022.day15;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Day15 implements Day {

    private static final Logger LOG = Logger.getLogger(Day15.class.getName());

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

    record Sensor(Point position, Point beacon) {

        int distanceToBeacon() {
            return position.distanceTo(beacon);
        }

        boolean covers(Point point) {
            return distanceToBeacon() >= position.distanceTo(point);
        }

        static Sensor parse(String string) {
            return new Sensor(
                Point.parse(string.substring(string.indexOf("Sensor at ") + "Sensor at ".length(), string.indexOf(':'))),
                Point.parse(string.substring(string.indexOf("closest beacon is at ") + "closest beacon is at ".length()))
            );
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

        Thing thingAt(Point point) {
            return things.get(point);
        }

        boolean hasCoverage(Point point) {
            return !things.containsKey(point) && sensors.stream().anyMatch(s -> s.covers(point));
        }

        char charOf(Point point) {
            var thing = thingAt(point);
            if (thing != null) {
                return switch (thing) {
                    case BEACON -> 'B';
                    case SENSOR -> 'S';
                };
            } else {
                return hasCoverage(point) ? '#' : '.';
            }
        }

        String toString(Point from, Point until) {
            var result = new StringBuilder();

            IntStream.rangeClosed(from.y, until.y).forEachOrdered(y -> {
                IntStream.rangeClosed(from.x, until.x).forEachOrdered(x -> result.append(charOf(new Point(x, y))));

                result.append('\n');
            });

            return result.toString();
        }

        int coveredLinesAtRow(int y) {
            var minX = sensors.stream().mapToInt(s -> Math.min(s.beacon.x, s.position.x)).min().orElseThrow();
            var maxX = sensors.stream().mapToInt(s -> Math.min(s.beacon.x, s.position.x)).max().orElseThrow();

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
        return input -> {
            var world = World.parse(input);

            LOG.info(() -> "\n" + world.toString(new Point(-4, 0), new Point(25, 22)));

            return world.coveredLinesAtRow(10) + ", " + world.coveredLinesAtRow(2000000);
        };
    }

    @Override
    public Assignment second() {
        return input -> "";
    }
}
