package nl.q8p.aoc2022.day02;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import static java.util.Arrays.stream;

public class Day02 implements Day {

    @Override
    public Assignment first() {
        return input -> stream(input.split("\\n")) // plays
                .map(play -> stream(play.split(" ")).mapToInt(s -> s.charAt(0)).toArray()) // two chars in an array
                .mapToInt(play ->
                        play[1] - 'X' + 1 + // points for the move
                        (((play[1] - 'X') - (play[0] - 'A') + 3 + 1) % 3) * 3) // points for winning, loosing or draw
                .sum();
    }

    @Override
    public Assignment second() {
        return input -> stream(input.split("\\n")) // plays
                .map(play ->play.split(" ")) // two strings in an array
                .mapToInt(play ->
                        switch (play[1]) {
                            case "X" -> switch (play[0]) {
                                case "A" -> 3;
                                case "B" -> 1;
                                default -> 2;
                            };
                            case "Y" -> 3 + switch (play[0]) {
                                case "A" -> 1;
                                case "B" -> 2;
                                default -> 3;
                            };
                            default -> 6 + switch (play[0]) {
                                case "A" -> 2;
                                case "B" -> 3;
                                default -> 1;
                            };
                        }
                )
                .sum();
    }
}
