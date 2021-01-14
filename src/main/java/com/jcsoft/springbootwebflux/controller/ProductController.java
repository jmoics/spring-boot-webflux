package com.jcsoft.springbootwebflux.controller;

import com.jcsoft.springbootwebflux.model.document.Product;
import com.jcsoft.springbootwebflux.model.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Controller
public class ProductController
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductRepository productRepository;

    @GetMapping({"/list", "/"})
    public String list(final Model model)
    {
        final Flux<Product> products = productRepository.findAll()
                                                        .map(prod -> {
                                                            prod.setName(prod.getName()
                                                                             .toUpperCase());
                                                            return prod;
                                                        });
        // aqui tenemos otro observador (el log)
        products.subscribe(prod -> LOG.info(prod.getName()));

        // quien se suscribe al flux para ser observador es la plantilla html para leer la lista
        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        return "list";
    }

    @GetMapping("/list-datadrive")
    public String listDataDrive(final Model model)
    {
        final Flux<Product> products = productRepository.findAll()
                                                        .map(prod -> {
                                                            prod.setName(prod.getName()
                                                                             .toUpperCase());
                                                            return prod;
                                                        })
                                                        .delayElements(Duration.ofSeconds(1));
        // aqui tenemos otro observador (el log)
        products.subscribe(prod -> LOG.info(prod.getName()));

        // forma para trabajar el back-pressure (contrapresion) y cargar data en bloques (cantidad de elementos)
        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 2));
        model.addAttribute("title", "Listado de Productos");
        return "list";
    }

    @GetMapping("/list-full")
    public String listFull(Model model)
    {
        final Flux<Product> products = productRepository.findAll()
                                                        .map(prod -> {
                                                            prod.setName(prod.getName()
                                                                             .toUpperCase());
                                                            return prod;
                                                        })
                                                        .repeat(5000);

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        return "list";
    }

    @GetMapping("/list-chunked")
    public String listChunked(Model model)
    {
        final Flux<Product> products = productRepository.findAll()
                                                        .map(prod -> {
                                                            prod.setName(prod.getName()
                                                                             .toUpperCase());
                                                            return prod;
                                                        })
                                                        .repeat(5000);

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        return "list-chunked";
    }
}
