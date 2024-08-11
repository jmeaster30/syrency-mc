package com.syrency.mc.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SelectRandomCollector<T> implements Collector<T, List<T>, Optional<T>> {
    Random rng;

    public SelectRandomCollector(Random rng) {
        this.rng = rng;
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<List<T>, Optional<T>> finisher() {
        return list -> list.isEmpty() ? Optional.empty() : Optional.ofNullable(list.get(rng.nextInt(list.size())));
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
