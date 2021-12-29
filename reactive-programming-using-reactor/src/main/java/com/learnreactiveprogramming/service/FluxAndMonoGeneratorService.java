package com.learnreactiveprogramming.service;

import java.io.PrintStream;
import java.time.Duration;
import java.util.List;
import java.util.function.*;

import reactor.core.publisher.*;

public class FluxAndMonoGeneratorService {

    // publisher
    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .log();
    }

    // publisher
    public Flux<String> namesFluxMap() {
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .map(String::toUpperCase)
                .log();
    }

    // publisher
    public Flux<String> namesFluxImmutable() {
        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "bobby"));
        namesFlux.map(String::toUpperCase)
                .log();

        return namesFlux;
    }

    // publisher
    public Flux<String> namesFluxFilter(int length) {
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .map(String::toUpperCase)
                .filter(name -> name.length() > length)
                .map(string -> String.format("%s-%s", string.length(), string))
                .log();

    }

    // publisher
    public Flux<String> namesFluxFlatMap() {
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .map(String::toUpperCase)
                .filter(name -> name.length() > 3)
                .flatMap(this::getFluxFromString)
                .log();

    }

    // publisher
    public Flux<String> namesFluxFlatMapAsync() {
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .map(String::toUpperCase)
                .filter(name -> name.length() > 3)
                .flatMap(this::getFluxFromStringDelay) // one to many transformations
                .log();

    }

    // publisher
    public Flux<String> namesFluxFlatConcatMap() { // when ordering matters for async
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .map(String::toUpperCase)
                .filter(name -> name.length() > 3)
                .concatMap(this::getFluxFromStringDelay) // one to many transformations but ordering is preserved
                .log();
    }

    // publisher
    public Flux<String> namesFluxTransform() {

        UnaryOperator<Flux<String>> filterMap = input -> input.map(String::toUpperCase)
                .filter(inputUpper -> inputUpper.length() > 3);
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .transform(filterMap)
                .flatMap(this::getFluxFromString)
                .log();

    }

    public Flux<String> namesFluxTransformDefaultIfEmpty() {

        UnaryOperator<Flux<String>> filterMap = input -> input.map(String::toUpperCase)
                .filter(inputUpper -> inputUpper.length() > 6);
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .transform(filterMap)
                .flatMap(this::getFluxFromString)
                .defaultIfEmpty("Default")
                .log();

    }

    public Flux<String> namesFluxTransformSwitchIfEmpty() {

        UnaryOperator<Flux<String>> filterMap = input -> input.map(String::toUpperCase)
                .filter(inputUpper -> inputUpper.length() > 6)
                .flatMap(this::getFluxFromString);

        Flux<String> defaultFlux = Flux.just("default").transform(filterMap);
        return Flux.fromIterable(List.of("alex", "ben", "bobby"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log();

    }

    // concat and concatwith processes in sequence and ONLY deals with Flux
    public Flux<String> exploreConcat() {
        var fluxABC = Flux.just("A", "b", "c");
        var fluxDEF = Flux.just("d", "e", "f");

        return Flux.concat(fluxABC, fluxDEF).log();
    }

    public Flux<String> exploreConcatWith() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("f");

        return aMono.concatWith(bMono);
    }

    public Flux<String> exploreMerge() {
        var fluxABC = Flux.just("a", "b", "c").delayElements(Duration.ofMillis(100));
        var fluxDEF = Flux.just("d", "e", "f").delayElements(Duration.ofMillis(125));

        return Flux.merge(fluxABC, fluxDEF).log();
    }

    public Flux<String> exploreMergeWith() {
        var fluxABC = Flux.just("a", "b", "c").delayElements(Duration.ofMillis(100));
        var fluxDEF = Flux.just("d", "e", "f").delayElements(Duration.ofMillis(125));

        return fluxABC.mergeWith(fluxDEF).log();
    }

    public Flux<String> exploreMergeWithMono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("f");

        return aMono.mergeWith(bMono);
    }

    public Flux<String> exploreMergeSequential() {
        var fluxABC = Flux.just("a", "b", "c").delayElements(Duration.ofMillis(100));
        var fluxDEF = Flux.just("d", "e", "f").delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(fluxABC, fluxDEF).log();
    }

    public Flux<String> exploreZip() {
        var fluxABC = Flux.just("a", "b", "c");
        var fluxDEF = Flux.just("d", "e", "f");

        return Flux.zip(fluxABC, fluxDEF, (one, two) -> one + two).log();
    }

    public Flux<String> exploreZipTuple4() {
        var fluxABC = Flux.just("a", "b", "c");
        var fluxDEF = Flux.just("d", "e", "f");
        var flux123 = Flux.just("1", "2", "3");
        var flux456 = Flux.just("4", "5", "6");

        return Flux.zip(fluxABC, fluxDEF, flux123, flux456).map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> exploreZipWith() {
        var fluxABC = Flux.just("a", "b", "c");
        var fluxDEF = Flux.just("d", "e", "f");

        return fluxABC.zipWith(fluxDEF).map(t2 -> t2.getT1() + t2.getT2()).log();
    }

    public Mono<String> exploreZipWithMono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("f");

        return aMono.zipWith(bMono).map(t2 -> t2.getT1() + t2.getT2());
    }

    // publisher
    public Mono<List<String>> namesMonoFlatMap() {

        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                .flatMap(this::splitStringMono).log(); // When you have a function which takes a mono, but returns
                                                       // another
        // mono, use flatmap
    }

    // publisher
    public Flux<String> namesMonoFlatMapMany() {

        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > 3)
                .flatMapMany(this::getFluxFromString).log(); // When you have a function which takes a mono, but returns
        // another
    }

    private Mono<List<String>> splitStringMono(String s) {
        return Mono.just(List.of(s.split("")));
    }

    public Flux<String> getFluxFromString(String string) {
        return Flux.fromArray(string.split(""));
    }

    public Flux<String> getFluxFromStringDelay(String string) {
        return Flux.fromArray(string.split("")).delayElements(Duration.ofMillis(1000));
    }

    // publisher
    public Mono<String> namesMono() {
        return Mono.just("alex").log();
    }

    public static void main(String... args) {

        FluxAndMonoGeneratorService service = new FluxAndMonoGeneratorService();

        service.namesFlux().subscribe(System.out::println);

        service.namesMono().subscribe(System.out::println);
    }
}
