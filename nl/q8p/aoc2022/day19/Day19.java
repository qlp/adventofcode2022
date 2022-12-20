package nl.q8p.aoc2022.day19;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Day19 implements Day {

    private static final Logger LOG = Logger.getLogger(Day19.class.getName());

    static Long parseResource(String string, String resourceName) {
        var stringThatEndsWithValue = string.substring(0, string.lastIndexOf(" " + resourceName));

        return Long.parseLong(stringThatEndsWithValue.substring(stringThatEndsWithValue.lastIndexOf(' ') + 1));
    }

    record OreRobot(long ore) {
        boolean canCreateWith(Resources resources) {
            return ore <= resources.ore;
        }

        public static OreRobot parse(String string) {
            return new OreRobot(parseResource(string, "ore"));
        }
    }

    record ClayRobot(long ore) {

        boolean canCreateWith(Resources resources) {
            return ore <= resources.ore;
        }

        public static ClayRobot parse(String string) {
            return new ClayRobot(parseResource(string, "ore"));
        }

    }

    record ObsidianRobot(long ore, long clay) {
        boolean canCreateWith(Resources resources) {
            return ore <= resources.ore && clay <= resources.clay;
        }

        public static ObsidianRobot parse(String string) {
            return new ObsidianRobot(parseResource(string, "ore"), parseResource(string, "clay"));
        }
    }

    record GeodeRobot(long ore, long obsidian) {
        boolean canBuyEveryTimeWith(Robots robots) {
            return ore <= robots.ore && obsidian <= robots.obsidian;
        }

        boolean canCreateWith(Resources resources) {
            return ore <= resources.ore && obsidian <= resources.obsidian;
        }

        public static GeodeRobot parse(String string) {
            return new GeodeRobot(parseResource(string, "ore"), parseResource(string, "obsidian"));
        }
    }

    record Blueprint(Long id, OreRobot oreRobot, ClayRobot clayRobot, ObsidianRobot obsidianRobot, GeodeRobot geodeRobot) {
        static Blueprint parse(String string) {
            var robotString = string.split("\\. ");
            return new Blueprint(
                Long.parseLong(string.substring(string.indexOf(' ') + 1, string.indexOf(":"))),
                OreRobot.parse(robotString[0]),
                ClayRobot.parse(robotString[1]),
                ObsidianRobot.parse(robotString[2]),
                GeodeRobot.parse(robotString[3])
            );
        }

        public String toString() {
            return "Blueprint " + id + ":\n" +
                    "  Each ore robot costs " + oreRobot.ore + " ore.\n" +
                    "  Each clay robot costs " + clayRobot.ore + " ore.\n" +
                    "  Each obsidian robot costs " + obsidianRobot.ore + " ore and " + obsidianRobot.clay + " clay.\n" +
                    "  Each geode robot costs " + geodeRobot.ore + " ore and " + geodeRobot.obsidian + " obsidian.";
        }
    }

    record Clock(long timeLeft) {
        public Clock tick() {
            return new Clock(timeLeft - 1);
        }

        public boolean outOfTime() {
            return timeLeft == 0;
        }
    }

    record Resources(long ore, long clay, long obsidian, long geode) {

        public Resources withHarvestOf(Robots robots) {
            return new Resources(
                ore + robots.ore,
                clay + robots.clay,
                obsidian + robots.obsidian,
                geode + robots.geode
            );
        }

        public Resources unchanged() {
            return new Resources(ore, clay, obsidian, geode);
        }

        Resources buying(OreRobot robot) {
            return new Resources(ore - robot.ore, clay, obsidian, geode);
        }

        Resources buying(ClayRobot robot) {
            return new Resources(ore - robot.ore, clay, obsidian, geode);
        }

        Resources buying(ObsidianRobot robot) {
            return new Resources(ore - robot.ore, clay - robot.clay, obsidian, geode);
        }

        Resources buying(GeodeRobot robot) {
            return new Resources(ore - robot.ore, clay, obsidian - robot.obsidian, geode);
        }

        @Override
        public String toString() {
            return "" + ore + ", " + clay + ", " + obsidian + ", " + geode;
        }
    }

    record Robots(long ore, long clay, long obsidian, long geode) {

        Robots unchanged() {
            return new Robots(ore, clay, obsidian, geode);
        }

        Robots addOre() {
            return new Robots(ore + 1, clay, obsidian, geode);
        }

        Robots addClay() {
            return new Robots(ore, clay + 1, obsidian, geode);
        }

        Robots addObsidian() {
            return new Robots(ore, clay, obsidian + 1, geode);
        }

        Robots addGeode() {
            return new Robots(ore, clay, obsidian, geode + 1);
        }

        @Override
        public String toString() {
            return "" + ore + ", " + clay + ", " + obsidian + ", " + geode;
        }
    }

    record World(Blueprint blueprint, Clock clock, Resources resources, Robots robots) {
//
//        public long findMaxRecursive() {
//
//            long result = -1;
//            if (blueprint.geodeRobot.canBuyEveryTimeWith(robots)) {
//                result = resources.geode + LongStream.range(1, clock.timeLeft).map(i -> robots.geode + i).sum();
//            } else if (clock.outOfTime()) {
//                result = resources.geode;
//            }
//
//            if (clock.timeLeft < blueprint.geodeRobot.obsidian - resources.obsidian) {
//                // can never make enough obsidian
//                result = 0;
//            }
//            if (clock.timeLeft < blueprint.geodeRobot.ore - resources.ore) {
//                // can never make enough ore
//                result = 0;
//            }
//
//            if (result != -1) {
//                return result;
//            }
//
//            return nextWorlds().stream().mapToLong(World::findMaxRecursive).max().orElse(resources.geode);
//        }

        public long score() {
            return resources.geode;
        }

        public List<World> nextWorlds() {
            var result = new ArrayList<World>();

            var canBuyOre = blueprint.oreRobot.canCreateWith(resources);
            var canBuyClay = blueprint.clayRobot.canCreateWith(resources);
            var canBuyObsidian = blueprint.obsidianRobot.canCreateWith(resources);
            var canBuyGeode = blueprint.geodeRobot.canCreateWith(resources);

            if (canBuyGeode) {
                result.add(withGeode());
            }
            if (canBuyObsidian) {
                result.add(withObsidian());
            }
            if (canBuyClay) {
                result.add(withClay());
            }
            if (canBuyOre) {
                result.add(withOre());
            }

            result.add(unchanged());

            return result;
        }

        World unchanged() {
            return next(resources.withHarvestOf(robots).unchanged(), robots.unchanged());
        }

        World withOre() {
            return next(resources.withHarvestOf(robots).buying(blueprint.oreRobot), robots.addOre());
        }

        World withClay() {
            return next(resources.withHarvestOf(robots).buying(blueprint.clayRobot), robots.addClay());
        }

        World withObsidian() {
            return next(resources.withHarvestOf(robots).buying(blueprint.obsidianRobot), robots.addObsidian());
        }

        World withGeode() {
            return next(resources.withHarvestOf(robots).buying(blueprint.geodeRobot), robots.addGeode());
        }

        World next(Resources newResources, Robots newRobots) {
            return new World(blueprint, clock.tick(), newResources, newRobots);
        }

        @Override
        public String toString() {
            return "" + blueprint.id + " robots: " + robots + " resources: " + resources;
        }
    }

    public long findMax(World start) {

        Set<World> worlds = new HashSet<>(List.of(start));
        var maxWorldComparing = 10000;

        for (int minute = 0; minute < start.clock.timeLeft; minute++) {
            LOG.info("minute: " + minute + ", world count: " + worlds.size());
            worlds.stream()
                    .sorted(Comparator.comparing(world -> -world.score()))
                    .limit(20)
                    .toList()
                    .stream()
                    .sorted(Comparator.comparingLong(world -> world.robots.ore))
                    .forEach(world ->
                    LOG.info("robots: " + world.robots + ", resources: " + world.resources + ": " + world.score()));

            var newWorlds = worlds.stream()
                    .flatMap(world -> world.nextWorlds().stream())
                    .collect(Collectors.groupingBy(world -> world.robots))
                    .values()
                    .stream()
                    .flatMap(w -> withMaxResources(w).stream())
                    .collect(Collectors.toSet());

            worlds = newWorlds.stream()
                    .sorted(Comparator.comparing(world -> -world.score()))
                    .limit(maxWorldComparing)
                    .collect(Collectors.toSet());
        }
        LOG.info("minute: LAST, world count: " + worlds.size());
        worlds.stream().sorted(Comparator.comparing(world -> -world.score())).limit(10).forEach(world ->
                LOG.info("robots: " + world.robots + ", resources: " + world.resources + ": " + world.score()));

        return worlds.stream().mapToLong(world -> world.resources.geode).max().orElse(0);
    }

    private List<World> withMaxResources(List<World> worlds) {
        var resources = worlds.stream().map(w -> w.resources).distinct().toList();

        var result = new ArrayList<Resources>();

        for (int i = 0; i < resources.size(); i++) {
            var candidate = resources.get(i);
            if (result.isEmpty()) {
                result.add(candidate);
            } else {
                boolean replaced = false;
                boolean useful = false;
                for (int j = 0; j < result.size(); j++) {
                    var competitor = result.get(j);

                    if (
                            candidate.ore >= competitor.ore &&
                            candidate.clay >= competitor.clay &&
                            candidate.obsidian >= competitor.obsidian &&
                            candidate.geode >= competitor.geode
                    ) {
                        result.set(j, candidate);
                        replaced = true;
                    } else if (
                            candidate.ore <= competitor.ore &&
                            candidate.clay <= competitor.clay &&
                            candidate.obsidian <= competitor.obsidian &&
                            candidate.geode <= competitor.geode
                    ) {
                        // useless
                    } else {
                        useful = true;
                    }
                }

                if (!replaced && useful) {
                    result.add(candidate);
                }
            }
        }

        return worlds.stream().filter(world -> result.contains(world.resources)).distinct().toList();
    }


    @Override
    public Assignment first() {
        return (run, input) -> {
            var blueprints = Arrays.stream(input.split("\\n")).map(Blueprint::parse).toList();

            var total = 0;
            for(var blueprint : blueprints) {
                LOG.info(blueprint::toString);

                var world = new World(blueprint, new Clock(24), new Resources(0L, 0L, 0L, 0L), new Robots(1L, 0L, 0L, 0L));

                long geodes = findMax(world);

                long qualityLevel = geodes * world.blueprint.id;

                LOG.info(() -> "blueprint: " + blueprint.id + ": " + geodes + " geodes => quality level: " + qualityLevel);

                total += qualityLevel;
            }

            return total;
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
