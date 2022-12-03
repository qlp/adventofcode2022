package nl.q8p.aoc2022.day03;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class Day03 implements Day {

    @Override
    public Assignment first() {
        return input -> stream(input.split("\\n"))
            .map(rucksack -> new String[] { rucksack.substring(0, rucksack.length() / 2), rucksack.substring(rucksack.length() / 2)})
            .mapToInt(compartments -> compartments[0].chars().filter(c0 -> compartments[1].chars().anyMatch(c1 -> c0 == c1)).findFirst().orElseThrow())
            .map(foundInBoth -> foundInBoth - (Character.isLowerCase((char)foundInBoth) ? 'a' - 1 : 'A' - 27))
            .sum();
    }

    @Override
    public Assignment second() {
        return input -> {
            var rucksacks = stream(input.split("\\n")).toList();

            var groups = new ArrayList<List<String>>();
            for (int index = 0; index < rucksacks.size(); index += 3) {
                groups.add(rucksacks.subList(index, index + 3));
            }

            return groups.stream()
                    .map(group -> group.stream()
                            .map(rucksack -> rucksack.chars().boxed().collect(Collectors.toSet()))
                            .toList())
                    .map(group -> {
                        group.get(0).retainAll(group.get(1));
                        group.get(0).retainAll(group.get(2));

                        return group.get(0);})
                    .mapToInt(foundInAll -> foundInAll.iterator().next())
                    .map(foundInAll -> foundInAll - (Character.isLowerCase((char)foundInAll) ? 'a' - 1 : 'A' - 27))
                    .sum();
        };
    }
}
