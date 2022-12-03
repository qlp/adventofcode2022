package nl.q8p.aoc2022.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class GroupCollector<T> implements Collector<T, List<List<T>>, List<List<T>>> {
    final int size;

    private GroupCollector(int size) {
        this.size = size;
    }

    public static <S> GroupCollector<S> withSize(int size) {
        return new GroupCollector<>(size);
    }

    @Override
    public Supplier<List<List<T>>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<List<T>>, T> accumulator() {
        return (result, element) -> {
            if (result.isEmpty() || result.get(result.size() - 1).size() == size) {
                result.add(new ArrayList<>());
            }

            result.get(result.size() - 1).add(element);
        };
    }

    @Override
    public BinaryOperator<List<List<T>>> combiner() {
        return (input, output) -> {
            throw new UnsupportedOperationException("parallel is not supported");
        };
    }

    @Override
    public Function<List<List<T>>, List<List<T>>> finisher() {
        return input -> input;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.IDENTITY_FINISH);
    }
}
