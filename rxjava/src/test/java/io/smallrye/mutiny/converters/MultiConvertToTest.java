package io.smallrye.mutiny.converters;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.converters.multi.RxConverters;
import io.smallrye.mutiny.converters.multi.ToCompletable;
import io.smallrye.mutiny.converters.multi.ToSingle;
import io.smallrye.mutiny.test.MultiAssertSubscriber;

public class MultiConvertToTest {

    @Test
    public void testCreatingACompletable() {
        Completable completable = Multi.createFrom().item(1).convert().with(new ToCompletable<>());
        assertThat(completable).isNotNull();
        completable.test().assertComplete();
    }

    @Test
    public void testThatSubscriptionOnCompletableProducesTheValue() {
        AtomicBoolean called = new AtomicBoolean();
        Completable completable = Multi.createFrom().deferred(() -> {
            called.set(true);
            return Multi.createFrom().item(2);
        }).convert().with(new ToCompletable<>());

        assertThat(completable).isNotNull();
        assertThat(called).isFalse();
        completable.test().assertComplete();
        assertThat(called).isTrue();
    }

    @Test
    public void testCreatingACompletableFromVoid() {
        Completable completable = Multi.createFrom().item(null).convert().with(new ToCompletable<>());
        assertThat(completable).isNotNull();
        completable.test().assertComplete();
    }

    @Test
    public void testCreatingACompletableWithFailure() {
        Completable completable = Multi.createFrom().failure(new IOException("boom")).convert().with(new ToCompletable<>());
        assertThat(completable).isNotNull();
        completable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingle() {
        Single<Optional<Integer>> single = Multi.createFrom().item(1).convert().with(RxConverters.toSingle());
        assertThat(single).isNotNull();
        single.test()
                .assertValue(Optional.of(1))
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleByConverter() {
        Single<Optional<Integer>> single = Multi.createFrom().item(1).convert().with(RxConverters.toSingle());
        assertThat(single).isNotNull();
        single.test()
                .assertValue(Optional.of(1))
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleFromNull() {
        Single<Integer> single = Multi.createFrom().item((Integer) null).convert()
                .with(ToSingle.onEmptyThrow(NoSuchElementException.class));
        assertThat(single).isNotNull();
        single
                .test()
                .assertFailure(NoSuchElementException.class)
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleFromNullWithConverter() {
        Single<Integer> single = Multi.createFrom().item((Integer) null).convert()
                .with(RxConverters.toSingleOnEmptyThrow(NoSuchElementException.class));
        assertThat(single).isNotNull();
        single
                .test()
                .assertFailure(NoSuchElementException.class)
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingASingleWithFailure() {
        Single<Optional<Integer>> single = Multi.createFrom().<Integer> failure(new IOException("boom")).convert()
                .with(RxConverters.toSingle());
        assertThat(single).isNotNull();
        single.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybe() {
        Maybe<Integer> maybe = Multi.createFrom().item(1).convert().with(RxConverters.toMaybe());
        assertThat(maybe).isNotNull();
        maybe.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybeFromNull() {
        Maybe<Integer> maybe = Multi.createFrom().item((Integer) null).convert().with(RxConverters.toMaybe());
        assertThat(maybe).isNotNull();
        maybe
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAMaybeWithFailure() {
        Maybe<Integer> maybe = Multi.createFrom().<Integer> failure(new IOException("boom")).convert()
                .with(RxConverters.toMaybe());
        assertThat(maybe).isNotNull();
        maybe.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservable() {
        Observable<Integer> observable = Multi.createFrom().item(1).convert().with(RxConverters.toObservable());
        assertThat(observable).isNotNull();
        observable.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservableFromNull() {
        Observable<Integer> observable = Multi.createFrom().item((Integer) null).convert().with(RxConverters.toObservable());
        assertThat(observable).isNotNull();
        observable
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAnObservableWithFailure() {
        Observable<Integer> observable = Multi.createFrom().<Integer> failure(new IOException("boom")).convert()
                .with(RxConverters.toObservable());
        assertThat(observable).isNotNull();
        observable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowable() {
        Flowable<Integer> flowable = Multi.createFrom().item(1).convert().with(RxConverters.toFlowable());
        assertThat(flowable).isNotNull();
        flowable.test()
                .assertValue(1)
                .assertComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableWithRequest() {
        AtomicBoolean called = new AtomicBoolean();
        MultiAssertSubscriber<Integer> subscriber = Multi.createFrom()
                .deferred(() -> Multi.createFrom().item(1).onItem().consume((item) -> called.set(true)))
                .convert().with(RxConverters.toFlowable())
                .subscribeWith(MultiAssertSubscriber.create(0));

        assertThat(called).isFalse();
        subscriber.assertHasNotReceivedAnyItem().assertSubscribed();
        subscriber.request(1);
        subscriber.assertCompletedSuccessfully().assertReceived(1);
        assertThat(called).isTrue();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableFromNull() {
        Flowable<Integer> flowable = Multi.createFrom().item((Integer) null).convert().with(RxConverters.toFlowable());
        assertThat(flowable).isNotNull();
        flowable
                .test()
                .assertComplete()
                .assertNoValues();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreatingAFlowableWithFailure() {
        Flowable<Integer> flowable = Multi.createFrom().<Integer> failure(new IOException("boom")).convert()
                .with(RxConverters.toFlowable());
        assertThat(flowable).isNotNull();
        flowable.test().assertError(e -> {
            assertThat(e).hasMessage("boom").isInstanceOf(IOException.class);
            return true;
        });
    }
}
