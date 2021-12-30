package com.reactivespring.random;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;

public class SinkTest {

    /// https://projectreactor.io/docs/core/release/reference/#processors
    @Test
    void sink1() {

        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        replaySink.emitNext(1, EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, EmitFailureHandler.FAIL_FAST);

        replaySink.asFlux().subscribe(i -> System.out.println("sub 1 -> " + i));

        replaySink.asFlux().subscribe(i -> System.out.println("sub 2 -> " + i));

        replaySink.tryEmitNext(4);

        replaySink.asFlux().subscribe(i -> System.out.println("sub 3 -> " + i));
        replaySink.asFlux().subscribe(i -> System.out.println("sub 4 -> " + i));
    }

    @Test
    void multiCast() {
        Sinks.Many<Integer> multicast = Sinks.many().multicast().onBackpressureBuffer();

        multicast.tryEmitNext(4);
        multicast.tryEmitNext(5);
        multicast.tryEmitNext(6);
        multicast.tryEmitNext(7);

        multicast.asFlux().subscribe(i -> System.out.println("sub 1 -> " + i));
        multicast.asFlux().subscribe(i -> System.out.println("sub 2 -> " + i));
        multicast.tryEmitNext(8);
    }

    @Test
    void uniCast() throws InterruptedException {
        Sinks.Many<Integer> multicast = Sinks.many().unicast().onBackpressureBuffer();

        multicast.tryEmitNext(4);
        multicast.tryEmitNext(5);
        multicast.tryEmitNext(6);
        multicast.tryEmitNext(7);

        multicast.asFlux().subscribe(i -> System.out.println("sub 1 -> " + i));
        // multicast.asFlux().subscribe(i -> System.out.println("sub 2 -> " + i));
        multicast.tryEmitNext(8);
        Thread.sleep(3000);
        multicast.tryEmitNext(9);
    }

}
