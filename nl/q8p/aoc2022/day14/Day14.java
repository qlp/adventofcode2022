package nl.q8p.aoc2022.day14;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Day14 implements Day {

    enum Tile {
        AIR,
        SAND,
        ROCK
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
        int height() {
            return maxY() + 1;
        }

        private int maxY() {
            return paths.stream().mapToInt(path -> path.points.stream().mapToInt(point -> point.y).max().orElseThrow()).max().orElseThrow();
        }

        static Paths parse(String string) {
            return new Paths(Arrays.stream(string.split("\n")).map(Path::parse).toList());
        }
    }

    static class Cave {
        public static final int WIDTH = 1000;
        private final Tile[][] tiles;

        Cave(Tile[][] tiles) {
            this.tiles = tiles;
        }

        Cave(Paths paths) {
            tiles = new Tile[paths.height()][];

            IntStream.range(0, paths.height()).forEach(y ->
                    tiles[y] = IntStream.range(0, WIDTH).mapToObj(x -> Tile.AIR).toArray(Tile[]::new)
            );

            paths.paths.forEach(path -> {
                Point previous = null;

                for(var point : path.points) {
                    if (previous != null) {
                        final Point from = previous;
                        IntStream.rangeClosed(Math.min(from.x, point.x), Math.max(from.x, point.x)).forEach(x ->
                                IntStream.rangeClosed(Math.min(from.y, point.y), Math.max(from.y, point.y)).forEach(y ->
                                        set(x, y, Tile.ROCK)
                                )
                        );
                    }
                    previous = point;
                }
            });
        }

        Cave withFloor() {
            var newTiles = new Tile[tiles.length + 2][];
            IntStream.range(0, tiles.length).forEach(y -> newTiles[y] = tiles[y]);

            newTiles[tiles.length] = IntStream.range(0, tiles[0].length).mapToObj(x -> Tile.AIR).toArray(Tile[]::new);
            newTiles[tiles.length + 1] = IntStream.range(0, tiles[0].length).mapToObj(x -> Tile.ROCK).toArray(Tile[]::new);

            return new Cave(newTiles);
        }

        void set(int x, int y, Tile tile) {
            tiles[y][x] = tile;
        }

        Tile get(Point point) {
            return get(point.x, point.y);
        }

        Tile get(int x, int y) {
            return tiles[y][x];
        }

        boolean addSand() {
            var previousPoint = new Point(500, 0);

            if (get(previousPoint) != Tile.AIR) {
                return false;
            }

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

            set(nextPoint.x, nextPoint.y, Tile.SAND);

            return true;
        }

        private Point calculateNextPoint(Point from) {
            if (from.y + 1 == tiles.length) {
                return null;
            }

            if (get(from.below()) == Tile.AIR) {
                return from.below();
            } else if (get(from.leftBelow()) == Tile.AIR) {
                return from.leftBelow();
            } else if (get(from.rightBelow()) == Tile.AIR) {
                return from.rightBelow();
            } else {
                return from;
            }
        }

        static Cave parse(String string) {
            return new Cave(Paths.parse(string));
        }
    }

    @Override
    public Assignment first() {
        return (run, input) -> {
            var cave = Cave.parse(input);

            int sandAdded = 0;

            while(cave.addSand()) {
                sandAdded++;
            }

            return sandAdded;
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> {
            var cave = Cave.parse(input).withFloor();

            int sandAdded = 0;

            while(cave.addSand()) {
                sandAdded++;
            }

            return sandAdded;
        };
    }
}
