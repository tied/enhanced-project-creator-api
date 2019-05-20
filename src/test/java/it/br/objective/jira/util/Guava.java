package it.br.objective.jira.util;

import java.util.function.BinaryOperator;
import java.util.stream.Collector;

import com.google.common.annotations.Beta;
import com.google.common.collect.Table;

public class Guava {

    @Beta
    public static <T, R, C, V, I extends Table<R, C, V>> Collector<T, ?, I> toTable(
            java.util.function.Function<? super T, ? extends R> rowFunction,
            java.util.function.Function<? super T, ? extends C> columnFunction,
            java.util.function.Function<? super T, ? extends V> valueFunction,
            java.util.function.Supplier<I> tableSupplier) {
        return Collector.of(
                tableSupplier
                , (table, input) -> table.put(rowFunction.apply(input), columnFunction.apply(input), valueFunction.apply(input))
                , throwingMerger());
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u, v) -> {
            throw new IllegalStateException(String.format("Duplicate key %s", u));
        };
    }
}
