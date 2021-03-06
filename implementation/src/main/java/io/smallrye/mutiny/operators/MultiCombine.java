package io.smallrye.mutiny.operators;

import java.util.List;
import java.util.function.Function;

import org.reactivestreams.Publisher;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.ParameterValidation;
import io.smallrye.mutiny.operators.multi.builders.CollectionBasedMulti;

public class MultiCombine {

    private MultiCombine() {
        // avoid direct instantiation.
    }

    public static <T> Multi<T> merge(List<Publisher<T>> participants, boolean collectFailures, int requests,
            int concurrency) {
        List<Publisher<T>> candidates = ParameterValidation.doesNotContainNull(participants, "participants");
        if (collectFailures) {
            return new CollectionBasedMulti<>(candidates)
                    .onItem().flatMap().multi(Function.identity()).collectFailures().withRequests(requests)
                    .mergeResults(concurrency);
        } else {
            return new CollectionBasedMulti<>(candidates)
                    .onItem().flatMap().multi(Function.identity()).withRequests(requests).mergeResults(concurrency);
        }
    }

    public static <T> Multi<T> concatenate(List<Publisher<T>> participants, boolean collectFailures, int requests) {
        List<Publisher<T>> candidates = ParameterValidation.doesNotContainNull(participants, "participants");
        if (collectFailures) {
            return new CollectionBasedMulti<>(candidates)
                    .onItem().flatMap().multi(Function.identity()).collectFailures().withRequests(requests)
                    .mergeResults(1);
        } else {
            return new CollectionBasedMulti<>(candidates)
                    .onItem().flatMap().multi(Function.identity()).collectFailures().withRequests(requests)
                    .mergeResults(1);
        }
    }

}
