package com.jcsoft.springbootwebflux.model.repository;

import com.jcsoft.springbootwebflux.model.document.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ProductRepository
        extends ReactiveMongoRepository<Product, String>
{
}
