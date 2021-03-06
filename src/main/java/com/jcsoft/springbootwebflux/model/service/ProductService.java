package com.jcsoft.springbootwebflux.model.service;

import com.jcsoft.springbootwebflux.model.document.Category;
import com.jcsoft.springbootwebflux.model.document.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductService
{
    Flux<Product> findAll();

    Flux<Product> findAllWithNameUpperCase();

    Flux<Product> findAllWithNameUpperCaseRepeat();

    Mono<Product> findById(String id);

    Mono<Product> save(Product product);

    Mono<Void> delete(Product product);

    Flux<Category> findCatAll();

    Mono<Category> findCatById(String id);

    Mono<Category> saveCat(Category category);
}
