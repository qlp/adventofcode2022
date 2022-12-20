package nl.q8p.aoc2022.day20;

import nl.q8p.aoc2022.Assignment;
import nl.q8p.aoc2022.Day;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Day20 implements Day {

    record Number(int index, long value) {
        @Override
        public String toString() {
            return Long.toString(value);
        }
    }

    private long decode(String input, long decryptionKey, int rounds) {
        var original = Arrays.stream(input.split("\\n")).map(Integer::parseInt).map(i -> decryptionKey * i).toList();
        int size = original.size();
        int sizeWhenMoving = size - 1;

        var numbers = new ArrayList<>(IntStream.range(0, original.size()).mapToObj(i -> new Number(i, original.get(i))).toList());

        IntStream.range(0, rounds).forEach( round ->
            IntStream.range(0, size).forEach( index -> {
                var number = numbers.stream().filter(n -> n.index == index).findFirst().orElseThrow();

                var oldIndex = numbers.indexOf(number);
                var newIndex = ((oldIndex + number.value) % sizeWhenMoving + sizeWhenMoving) % sizeWhenMoving;

                numbers.remove(oldIndex);
                numbers.add((int) newIndex, number);
            })
        );

        var numberWithZero = numbers.stream().filter(n -> n.value == 0).findFirst().orElseThrow();
        int indexOfZero = numbers.indexOf(numberWithZero);

        return LongStream.of(1000, 2000, 3000).map(i -> numbers.get((int)((indexOfZero + i) % size)).value).sum();
    }

    @Override
    public Assignment first() {
        return (run, input) -> decode(input, 1, 1);
    }

    @Override
    public Assignment second() {
        return (run, input) -> decode(input, 811589153L, 10);
    }
}
