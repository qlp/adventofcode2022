package nl.q8p.aoc2022.day14;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day14 implements Day {

    private static Logger LOG = Logger.getLogger(Day14.class.getName());

    enum Tile {
        air,
        sand,
        rock
    }

    record Point(int x, int y) {
        static Point parse(String string) {
            var segments = Arrays.stream(string.split(",")).map(Integer::parseInt).toList();
            return new Point(segments.get(0), segments.get(1));
        }

        Point below() {
            return new Point(x, y + 1);
        }

        Point leftBelow() {
            return new Point(x - 1, y + 1);
        }

        Point rightBelow() {
            return new Point(x + 1, y + 1);
        }
    }

    record Path(List<Point> points) {
        static Path parse(String string) {
            return new Path(Arrays.stream(string.split(" -> ")).map(Point::parse).toList());
        }
    }

    record Paths(List<Path> paths) {
        int width() {
            return (maxX() - minX() + 1);
        }

        int heigth() {
            return maxY() - minY() + 1;
        }

        private int minX() {
            return paths.stream().mapToInt(path -> path.points.stream().mapToInt(point -> point.x).min().orElseThrow()).min().orElseThrow();
        }

        private int maxX() {
            return paths.stream().mapToInt(path -> path.points.stream().mapToInt(point -> point.x).max().orElseThrow()).max().orElseThrow();
        }

        private int minY() {
            return 0;
        }

        private int maxY() {
            return paths.stream().mapToInt(path -> path.points.stream().mapToInt(point -> point.y).max().orElseThrow()).max().orElseThrow();
        }

        static Paths parse(String string) {
            return new Paths(Arrays.stream(string.split("\n")).map(Path::parse).toList());
        }
    }

    static class Cave {
        private final Tile[][] tiles;

        private final int offsetX;

        Cave(Paths paths) {
            tiles = new Tile[paths.heigth()][];

            var width = 1000;
            offsetX = 0;

            IntStream.range(0, paths.heigth()).forEach(y -> {
                tiles[y] = IntStream.range(0, width).mapToObj(x -> Tile.air).toArray(Tile[]::new);
            });

            paths.paths.forEach(path -> {
                Point previous = null;

                for(var point : path.points) {
                    if (previous != null) {
                        final Point from = previous;
                        IntStream.rangeClosed(Math.min(from.x, point.x), Math.max(from.x, point.x)).forEach(x ->
                                IntStream.rangeClosed(Math.min(from.y, point.y), Math.max(from.y, point.y)).forEach(y ->
                                        set(x, y, Tile.rock)
                                )
                        );
                    }
                    previous = point;
                }
            });
        }

        void set(int x, int y, Tile tile) {
            tiles[y][x - offsetX] = tile;
        }

        Tile get(Point point) {
            return get(point.x, point.y);
        }

        Tile get(int x, int y) {
            return tiles[y][x - offsetX];
        }

        boolean addSand() {
            var previousPoint = new Point(500, 0);
            Point nextPoint = null;
            do {
                if (nextPoint != null) {
                    previousPoint = nextPoint;
                }
                nextPoint = calculateNextPoint(previousPoint);
            } while (nextPoint != null && !previousPoint.equals(nextPoint));

            if (nextPoint == null) {
                return false;
            }

            set(nextPoint.x, nextPoint.y, Tile.sand);

            return true;
        }

        private Point calculateNextPoint(Point from) {
            if (from.y + 1 == tiles.length) {
                return null;
            }

            if (get(from.below()) == Tile.air) {
                return from.below();
            } else if (get(from.leftBelow()) == Tile.air) {
                return from.leftBelow();
            } else if (get(from.rightBelow()) == Tile.air) {
                return from.rightBelow();
            } else {
                return from;
            }
        }


        @Override
        public String toString() {
            return Arrays.stream(tiles).map(row -> Arrays.stream(row).map(tile -> switch(tile) {
                case air -> '.';
                case rock -> '#';
                case sand -> 'O';
            }).collect(Collector.of(
                    StringBuilder::new,
                    StringBuilder::append,
                    StringBuilder::append,
                    StringBuilder::toString))).collect(Collectors.joining("\n"));
        }

        static Cave parse(String string) {
            return new Cave(Paths.parse(string));
        }
    }

    @Override
    public Assignment first() {
        return input -> {
            var cave = Cave.parse(input);

            int sandAdded = 0;

            while(cave.addSand()) {
                sandAdded++;
//                LOG.info("cave:");
//                LOG.info("\n" + cave);
            }

            return sandAdded;
        };
    }

    @Override
    public Assignment second() {
        return input -> "";
    }
}
