package nl.q8p.aoc2022.day03;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;
import nl.q8p.aoc2022.utils.GroupCollector;

import java.util.HashSet;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class Day03 implements Day {

    @Override
    public Assignment first() {
        return (run, input) -> stream(input.split("\\n"))
            .map(rucksack -> new String[] { rucksack.substring(0, rucksack.length() / 2), rucksack.substring(rucksack.length() / 2)})
            .mapToInt(compartments -> compartments[0].chars().filter(c0 -> compartments[1].chars().anyMatch(c1 -> c0 == c1)).findFirst().orElseThrow())
            .map(foundInBoth -> foundInBoth - (Character.isLowerCase((char)foundInBoth) ? 'a' - 1 : 'A' - 27))
            .sum();
    }

    @Override
    public Assignment second() {
        return (run, input) -> stream(input.split("\\n"))
            .map(rucksack -> rucksack.chars().boxed().collect(Collectors.toSet()))
            .collect(GroupCollector.withSize(3))
            .stream()
            .map(group -> group.stream()
                    .reduce((one, other) -> {
                        var result = new HashSet<>(one);
                        result.retainAll(other);
                        return result;
                    })
                    .orElseThrow())
            .mapToInt(foundInAll -> foundInAll.iterator().next())
            .map(foundInAll -> foundInAll - (Character.isLowerCase((char)foundInAll) ? 'a' - 1 : 'A' - 27))
            .sum();
    }
}
