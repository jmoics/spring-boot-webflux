package com.jcsoft.springbootwebflux.model.repository;

import com.jcsoft.springbootwebflux.model.document.Category;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface CategoryRepository
        extends ReactiveMongoRepository<Category, String>
{
}
