package com.jcsoft.springbootwebflux;

import com.jcsoft.springbootwebflux.model.document.Product;
import com.jcsoft.springbootwebflux.model.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@SpringBootApplication
public class SpringBootWebfluxApplication
        implements CommandLineRunner
{
    private static final Logger LOG = LoggerFactory.getLogger(SpringBootWebfluxApplication.class);
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    public static void main(String[] args)
    {
        SpringApplication.run(SpringBootWebfluxApplication.class, args);
    }

    @Override
    public void run(String... args)
            throws Exception
    {
        reactiveMongoTemplate.dropCollection("product").subscribe();

        Flux.just(Product.builder().name("TV Samsung LED").price(550.5).build(),
                  Product.builder().name("Camara Sony 4K").price(1220.5).build(),
                  Product.builder().name("Apple iPod").price(980.2).build(),
                  Product.builder().name("Sony Notebook").price(1125.5).build(),
                  Product.builder().name("HP Multifuncional").price(425.3).build(),
                  Product.builder().name("Bianchi Bicicleta").price(125.2).build(),
                  Product.builder().name("Dell Latitude 7400").price(1428.4).build(),
                  Product.builder().name("TV Sony Bravia OLED").price(1800.5).build())
            //el save devuelve Mono y queremos el product, asi que el flatMap convierte los elementos de cada Mono en un solo Flux que contiene products
            .flatMap(prod -> {
                prod.setCreateAt(LocalDate.now());
                return productRepository.save(prod);
            })
            .subscribe(prod -> LOG.info("Insert: {} - {}", prod.getId(), prod.getName()));
    }
}
