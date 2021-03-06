package io.smallrye.mutiny.operators.multi;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.ParameterValidation;
import io.smallrye.mutiny.helpers.Subscriptions;
import io.smallrye.mutiny.subscription.SerializedSubscriber;

/**
 * Skips items emitted by the upstream until the other publisher emits either an item or completes.
 *
 * @param <T> the type of items emitted by the upstream (and propagated downstream)
 * @param <U> the type of items emitted by the other publisher
 */
public final class MultiSkipUntilPublisherOp<T, U> extends AbstractMultiOperator<T, T> {

    private final Publisher<U> other;

    public MultiSkipUntilPublisherOp(Multi<? extends T> upstream, Publisher<U> other) {
        super(upstream);
        this.other = ParameterValidation.nonNull(other, "other");
    }

    @Override
    public void subscribe(Subscriber<? super T> actual) {
        SkipUntilMainProcessor<T> main = new SkipUntilMainProcessor<>(actual);
        OtherStreamTracker<U> otherSubscriber = new OtherStreamTracker<>(main);
        other.subscribe(otherSubscriber);
        upstream.subscribe(main);
    }

    @SuppressWarnings("SubscriberImplementation")
    static final class OtherStreamTracker<U> implements Subscriber<U> {

        private final SkipUntilMainProcessor<?> main;

        OtherStreamTracker(SkipUntilMainProcessor<?> main) {
            this.main = main;
        }

        @Override
        public void onSubscribe(Subscription s) {
            main.setOtherSubscription(s);
        }

        @Override
        public void onNext(U t) {
            main.open();
        }

        @Override
        public void onError(Throwable t) {
            if (!main.isOpened()) {
                main.onError(t);
            }
        }

        @Override
        public void onComplete() {
            main.open();
        }

    }

    static final class SkipUntilMainProcessor<T> extends MultiOperatorProcessor<T, T> {

        private AtomicReference<Subscription> other = new AtomicReference<>();
        private AtomicBoolean gate = new AtomicBoolean(false);

        SkipUntilMainProcessor(Subscriber<? super T> downstream) {
            super(new SerializedSubscriber<>(downstream));
        }

        void open() {
            if (gate.compareAndSet(false, true)) {
                Subscriptions.cancel(other);
            }
        }

        boolean isOpened() {
            return gate.get();
        }

        void setOtherSubscription(Subscription s) {
            if (other.compareAndSet(null, s)) {
                s.request(1);
            } else {
                s.cancel();
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            Subscriptions.cancel(other);
        }

        @Override
        public void onNext(T t) {
            if (gate.get()) {
                downstream.onNext(t);
            } else {
                request(1);
            }
        }

        @Override
        public void onError(Throwable t) {
            Subscriptions.cancel(other);
            super.onError(t);
        }

        @Override
        public void onComplete() {
            Subscriptions.cancel(other);
            super.onComplete();
        }
    }
}
