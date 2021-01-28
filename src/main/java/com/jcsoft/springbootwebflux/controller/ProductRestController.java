package com.jcsoft.springbootwebflux.controller;

import com.jcsoft.springbootwebflux.model.document.Product;
import com.jcsoft.springbootwebflux.model.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Rest API without html template
 */
@RestController
@RequestMapping("/api/products")
public class ProductRestController
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductRestController.class);

    private ProductService productService;

    public ProductRestController(final ProductService productService)
    {
        this.productService = productService;
    }

    @GetMapping
    public Flux<Product> index()
    {
        Flux<Product> products = productService.findAll()
                                               .map(prod -> {
                                                   prod.setName(prod.getName()
                                                                    .toUpperCase());
                                                   return prod;
                                               })
                                               .doOnNext(prod -> LOG.info(prod.getName()));
        return products;
    }

    @GetMapping("/{id}")
    public Mono<Product> show(@PathVariable String id)
    {
        //Mono<Product> product = productService.findById(id);
        Mono<Product> product = productService.findAll()
                                              .filter(prod -> prod.getId()
                                                                  .equals(id))
                                              .next() //convierte el flux a un mono
                                              .doOnNext(prod -> LOG.info(prod.getName()));
        return product;
    }
}
