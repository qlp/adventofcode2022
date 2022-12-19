package nl.q8p.aoc2022.day19;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Day19 implements Day {

    private static final Logger LOG = Logger.getLogger(Day19.class.getName());

    record Robot(int ore, int clay, int obsidian) {

        @Override
        public String toString() {
            List<String> parts = new ArrayList<>();
            if (ore > 0) {
                parts.add("" + ore + " ore");
            }
            if (clay > 0) {
                parts.add("" + clay + " clay");
            }
            if (obsidian > 0) {
                parts.add("" + obsidian + " obsidian");
            }

            return String.join(" and ", parts);
        }

        static Robot parse(String string, String name) {
            var prefix = "Each " + name + " robot costs ";
            var beginOfRobotIndex = string.indexOf(prefix);
            var endOfRobotIndex = string.indexOf('.', beginOfRobotIndex);

            var robotDescription = string.substring(beginOfRobotIndex, endOfRobotIndex);

            return new Robot(
                parseResource(robotDescription, "ore"),
                parseResource(robotDescription, "clay"),
                parseResource(robotDescription, "obsidian"));
        }

        private static int parseResource(String string, String name) {
            var costs = string.substring(string.indexOf("costs"));
            int endOfCountIndex = costs.lastIndexOf(name) - 1;

            if (endOfCountIndex < 0) {
                return 0;
            }

            int beginOfCountIndex = costs.substring(0, endOfCountIndex).lastIndexOf(' ') + 1;

            return Integer.parseInt(costs.substring(beginOfCountIndex, endOfCountIndex));
        }
    }

    record Blueprint(int id, Robot ore, Robot clay, Robot obsidian, Robot geode) {

        @Override
        public String toString() {
            return "Blueprint " + id + ":\n" +
                "  Each ore robot costs " + ore + ".\n" +
                "  Each clay robot costs " + clay + ".\n" +
                "  Each obsidian robot costs " + obsidian + ".\n" +
                "  Each geode robot costs " + geode + ".\n";
        }

        static Blueprint parse(String string) {
            return new Blueprint(
                    parseId(string),
                    Robot.parse(string, "ore"),
                    Robot.parse(string, "clay"),
                    Robot.parse(string, "obsidian"),
                    Robot.parse(string, "geode")
                );
        }

        private static int parseId(String string) {
            return Integer.parseInt(string.substring(string.indexOf(' ') + 1, string.indexOf(':')));
        }
    }


    @Override
    public Assignment first() {
        return (run, input) -> {
            var blueprints = Arrays.stream(input.split("\\n")).map(Blueprint::parse).toList();

            blueprints.forEach(b -> LOG.info(() -> b.toString()));

            return "";
        };
    }

    @Override
    public Assignment second() {
        return (run, input) -> "";
    }
}
