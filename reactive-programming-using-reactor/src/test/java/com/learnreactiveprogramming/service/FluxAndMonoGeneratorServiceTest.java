package com.learnreactiveprogramming.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.*;
import reactor.test.StepVerifier;

class FluxAndMonoGeneratorServiceTest {

    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    public void nameFlux() {

        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFlux();
        StepVerifier.create(nameFlux).expectNext("alex", "ben", "bobby").verifyComplete();
    }

    @Test
    public void nameFluxMap() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxMap();
        StepVerifier.create(nameFlux).expectNext("alex".toUpperCase(), "ben".toUpperCase(), "bobby".toUpperCase())
                .verifyComplete();
    }

    @Test
    public void namesFluxImmutable() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxImmutable();
        StepVerifier.create(nameFlux).expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void namesFluxFilter() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxFilter(3);
        StepVerifier.create(nameFlux)
                .expectNext("4-alex".toUpperCase(), "5-bobby".toUpperCase())
                .verifyComplete();
    }

    @Test
    public void namesFluxFlatMap() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxFlatMap();
        StepVerifier.create(nameFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void namesFluxFlatMapAsync() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxFlatMapAsync();
        StepVerifier.create(nameFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void namesFluxFlaConcatMap() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxFlatConcatMap(); // concat map keeps/preserves
                                                                                      // order for async
                                                                                      // time to completion will take
                                                                                      // longer
        StepVerifier.create(nameFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void namesMonoFlatMap() {
        Mono<List<String>> nameMono = fluxAndMonoGeneratorService.namesMonoFlatMap();
        StepVerifier.create(nameMono)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    public void namesMonoFlatMapMany() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesMonoFlatMapMany();
        StepVerifier.create(nameFlux)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    public void namesFluxTransform() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxTransform(); // concat map keeps/preserves
                                                                                  // order for async
                                                                                  // time to completion will take
                                                                                  // longer
        StepVerifier.create(nameFlux)
                .expectNextCount(9)
                .verifyComplete();
    }

    @Test
    public void namesFluxTransformDefaultIfEmpty() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxTransformDefaultIfEmpty();
        StepVerifier.create(nameFlux)
                .expectNext("Default")
                .verifyComplete();
    }

    @Test
    public void namesFluxTransformSwitchIfEmpty() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty();
        StepVerifier.create(nameFlux)
                .expectNextCount(7)
                .verifyComplete();
    }

    @Test
    public void exloreConcat() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreConcat();
        StepVerifier.create(nameFlux)
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    public void exploreConcatWith() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreConcatWith();
        StepVerifier.create(nameFlux)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void exploreMerge() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreMerge();
        StepVerifier.create(nameFlux)
                .expectNext("a", "d", "b", "e", "c", "f")
                .verifyComplete();
    }

    @Test
    public void exploreMergeWith() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreMergeWith();
        StepVerifier.create(nameFlux)
                .expectNext("a", "d", "b", "e", "c", "f")
                .verifyComplete();
    }

    @Test
    public void exploreMergeWithMono() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreMergeWithMono();
        StepVerifier.create(nameFlux)
                .expectNext("A", "f")
                .verifyComplete();
    }

    @Test
    public void exploreMergeSequential() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreMergeSequential();
        StepVerifier.create(nameFlux)
                .expectNext("a", "b", "c", "d", "e", "f")
                .verifyComplete();
    }

    @Test
    public void exploreZip() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreZip();
        StepVerifier.create(nameFlux)
                .expectNext("ad", "be", "cf")
                .verifyComplete();
    }

    @Test
    public void exploreZipTuple4() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreZipTuple4();
        StepVerifier.create(nameFlux)
                .expectNext("ad14", "be25", "cf36")
                .verifyComplete();
    }

    @Test
    public void exploreZipWith() {
        Flux<String> nameFlux = fluxAndMonoGeneratorService.exploreZipWith();
        StepVerifier.create(nameFlux)
                .expectNext("ad", "be", "cf")
                .verifyComplete();
    }

    @Test
    public void exploreZipWithMono() {
        Mono<String> nameFlux = fluxAndMonoGeneratorService.exploreZipWithMono();
        StepVerifier.create(nameFlux)
                .expectNext("Af")
                .verifyComplete();
    }

}