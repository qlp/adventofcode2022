package nl.q8p.aoc2022.day19;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Day19 implements Day {

    private static final Logger LOG = Logger.getLogger(Day19.class.getName());

    record Robot(ResourceType type, Map<ResourceType, Long> costs) {

        boolean canBuild(Map<ResourceType, Long> robots) {
            return nonZero(costs).containsAll(nonZero(robots));
        }

        @Override
        public String toString() {
            return Arrays.stream(ResourceType.values())
                    .filter(resourceType -> resourceType != ResourceType.GEODE)
                    .filter(resourceType -> costs.getOrDefault(resourceType, 0L) > 0L)
                    .map(resourceType -> "" + costs.get(resourceType) + " " + resourceType.name().toLowerCase())
                    .collect(Collectors.joining(" and "));
        }

        static Robot parse(String string, String name) {
            var prefix = "Each " + name + " robot costs ";
            var beginOfRobotIndex = string.indexOf(prefix);
            var endOfRobotIndex = string.indexOf('.', beginOfRobotIndex);

            var robotDescription = string.substring(beginOfRobotIndex, endOfRobotIndex);

            return new Robot(
                ResourceType.valueOf(name.toUpperCase()),
                Arrays.stream(ResourceType.values())
                        .filter(resourceType -> resourceType != ResourceType.GEODE)
                        .collect(Collectors.toMap(
                                resourceType -> resourceType,
                                resourceType -> parseResource(robotDescription, resourceType.name().toLowerCase()))));
        }

        private static long parseResource(String string, String name) {
            var costs = string.substring(string.indexOf("costs"));
            int endOfCountIndex = costs.lastIndexOf(name) - 1;

            if (endOfCountIndex < 0) {
                return 0;
            }

            int beginOfCountIndex = costs.substring(0, endOfCountIndex).lastIndexOf(' ') + 1;

            return Long.parseLong(costs.substring(beginOfCountIndex, endOfCountIndex));
        }
    }

    enum ResourceType {
        ORE, CLAY, OBSIDIAN, GEODE
    }

    record Blueprint(int id, Map<ResourceType, Robot> robots) {

        @Override
        public String toString() {
            return "Blueprint " + id + ":\n" +
                "  Each ore robot costs " + robots.get(ResourceType.ORE) + ".\n" +
                "  Each clay robot costs " + robots.get(ResourceType.CLAY) + ".\n" +
                "  Each obsidian robot costs " + robots.get(ResourceType.OBSIDIAN) + ".\n" +
                "  Each geode robot costs " + robots.get(ResourceType.GEODE) + ".\n";
        }

        static Blueprint parse(String string) {
            return new Blueprint(
                    parseId(string),
                    Arrays.stream(ResourceType.values()).map(t -> Robot.parse(string, t.name().toLowerCase())).collect(Collectors.toMap(robot -> robot.type, robot -> robot))
                );
        }

        private static int parseId(String string) {
            return Integer.parseInt(string.substring(string.indexOf(' ') + 1, string.indexOf(':')));
        }
    }

    static class World {
        private final long timeLeft;

        private final Blueprint blueprint;

        private final Map<ResourceType, Long> resources;

        private final Map<ResourceType, Long> robots;

        World(Blueprint blueprint, long timeLeft) {
            this.timeLeft = timeLeft;
            this.blueprint = blueprint;
            resources = new EnumMap<>(ResourceType.class);
            robots = new EnumMap<>(ResourceType.class);
            robots.put(ResourceType.ORE, 1L);
        }

        World(Blueprint blueprint, long timeLeft, Map<ResourceType, Long> resources, Map<ResourceType, Long> robots) {
            this.timeLeft = timeLeft;
            this.blueprint = blueprint;
            this.resources = resources;
            this.robots = robots;
        }

        World tick() {
            return new World(blueprint,
                timeLeft - 1,
                Arrays.stream(ResourceType.values()).collect(Collectors.toMap(
                    resourceType -> resourceType,
                    resourceType -> resources.computeIfAbsent(resourceType, r -> 0L) + robots.computeIfAbsent(resourceType, r -> 0L))),
                robots
            );
        }

        private World build(Robot robot) {
            var ticks = nonZero(robot.costs)
                    .stream()
                    .mapToLong(resourceType -> ticksToHave(resourceType, robot.costs.get(resourceType)))
                    .max()
                    .orElse(0);

            var newResources = new EnumMap<>(resources);
            var newRobots = new EnumMap<>(robots);

            Arrays.stream(ResourceType.values()).forEach(resourceType -> newResources.put(resourceType,
                    newResources.getOrDefault(resourceType, 0L) + robots.getOrDefault(resourceType, 0L) * ticks));

            if (ticks < timeLeft) {
                Arrays.stream(ResourceType.values()).forEach(resourceType -> newResources.put(resourceType,
                        newResources.getOrDefault(resourceType, 0L) - robot.costs.getOrDefault(resourceType, 0L)));

                newRobots.put(robot.type, newRobots.getOrDefault(robot.type, 0L) + 1L);
            }

            return new World(blueprint,
                    Math.max(0L, timeLeft - ticks),
                    newResources,
                    newRobots
            );
        }

        private long ticksToHave(ResourceType resourceType, long amount) {
            var need = amount - resources.getOrDefault(resourceType, 0L);
            var producePerTick = robots.getOrDefault(resourceType, Long.MAX_VALUE);

            return Math.max(0, need  / producePerTick + (need % producePerTick == 0 ? 0 : 1));
        }

        boolean outOfTime() {
            return timeLeft == 0;
        }

        long findMax(ResourceType resourceType) {
            LOG.info(() -> "time: " + timeLeft + ", resource: " + resources + ", robots: " + robots);

            if (outOfTime()) {
                return resources.getOrDefault(resourceType, 0L);
            }

            return blueprint.robots
                    .values()
                    .stream()
                    .filter(robot -> robot.canBuild(robots))
                    .map(this::build)
                    .mapToLong(w -> w.findMax(resourceType))
                    .max()
                    .orElse(0L);
        }
    }

    static Set<ResourceType> nonZero(Map<ResourceType, Long> source) {
        return source.entrySet().stream().filter(e -> e.getValue() > 0L).map(Map.Entry::getKey).collect(Collectors.toSet());
    }


    @Override
    public Assignment first() {
        return (run, input) -> {
            var blueprints = Arrays.stream(input.split("\\n")).map(Blueprint::parse).toList();

            long total = 0L;

            for(var blueprint : blueprints) {
                LOG.info(blueprint::toString);

                var world = new World(blueprint, 30);

                long geodes = world.findMax(ResourceType.GEODE);

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
