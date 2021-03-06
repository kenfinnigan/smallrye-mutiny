package io.smallrye.mutiny.operators;

import org.junit.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.test.MultiAssertSubscriber;

public class MultiIgnoreTest {

    @Test
    public void test() {
        Multi.createFrom().items(1, 2, 3, 4)
                .onItem().ignore()
                .subscribe().withSubscriber(MultiAssertSubscriber.create(4))
                .assertCompletedSuccessfully()
                .assertHasNotReceivedAnyItem();
    }

    @Test
    public void testWithNever() {
        MultiAssertSubscriber<Void> subscriber = Multi.createFrom().nothing()
                .onItem().ignore()
                .subscribe().withSubscriber(MultiAssertSubscriber.create(4))
                .assertNotTerminated();

        subscriber.cancel();
    }
}
