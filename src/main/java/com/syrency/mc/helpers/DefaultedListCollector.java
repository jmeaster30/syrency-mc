package com.syrency.mc.helpers;

import net.minecraft.util.collection.DefaultedList;

import java.util.AbstractList;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DefaultedListCollector<T> implements Collector<T, DefaultedList<T>, DefaultedList<T>> {

    @Override
    public Supplier<DefaultedList<T>> supplier() {
        return DefaultedList::of;
    }

    @Override
    public BiConsumer<DefaultedList<T>, T> accumulator() {
        return AbstractList::add;
    }

    @Override
    public BinaryOperator<DefaultedList<T>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<DefaultedList<T>, DefaultedList<T>> finisher() {
        return (x) -> x;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }
}
