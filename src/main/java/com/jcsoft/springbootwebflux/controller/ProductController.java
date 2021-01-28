package com.jcsoft.springbootwebflux.controller;

import com.jcsoft.springbootwebflux.model.document.Category;
import com.jcsoft.springbootwebflux.model.document.Product;
import com.jcsoft.springbootwebflux.model.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * product session attribute is a special attribute stored temporarily and deleted in save method.
 */
@SessionAttributes("product")
@Controller
public class ProductController
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(final ProductService productService)
    {
        this.productService = productService;
    }

    @ModelAttribute("categoriees")
    public Flux<Category> categories()
    {
        return productService.findCatAll();
    }

    @GetMapping("/watch/{id}")
    public Mono<String> watch(final Model model,
                              @PathVariable final String id)
    {
        return productService.findById(id)
                             .doOnNext(prod -> {
                                 model.addAttribute("product", prod);
                                 model.addAttribute("title", "Detalle de Producto");
                             }).switchIfEmpty(Mono.just(Product.builder().build()))
                             .flatMap(prod -> {
                                 if (prod.getId() == null) {
                                     return Mono.error(new InterruptedException("No existe el producto"));
                                 }
                                 return Mono.just(prod);
                             }).thenReturn("watch")
                             .onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+el+producto"));
    }

    @GetMapping("/uploads/img/{namePhoto:.+}")
    public Mono<ResponseEntity<Resource>> showPhoto(@PathVariable final String namePhoto)
            throws MalformedURLException
    {
        final Path path = Paths.get("").resolve(namePhoto).toAbsolutePath();
        final Resource image = new UrlResource(path.toUri());

        return Mono.just(ResponseEntity.ok()
                                       .header(HttpHeaders.CONTENT_DISPOSITION,
                                               "attachment; filename=\"" + image.getFilename() + "\"")
                                       .body(image)
        );
    }

    @GetMapping({"/list", "/"})
    public Mono<String> list(final Model model)
    {
        final Flux<Product> products = productService.findAllWithNameUpperCase();
        // aqui tenemos otro observador (el log)
        products.subscribe(prod -> LOG.info(prod.getName()));

        // quien se suscribe al flux para ser observador es la plantilla html para leer la lista
        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");

        return Mono.just("list");
    }

    @GetMapping("/form")
    public Mono<String> create(final Model model)
    {
        model.addAttribute("product", Product.builder()
                                             .build());
        model.addAttribute("title", "Formulario de Producto");
        model.addAttribute("button", "Crear");
        return Mono.just("form");
    }

    @GetMapping("form/{id}")
    public Mono<String> edit(@PathVariable final String id,
                             final Model model)
    {
        final Mono<Product> productMono = productService.findById(id)
                                                        .doOnNext(prod -> {
                                                            LOG.info("Producto editado: {}", prod.getName());
                                                        })
                                                        .defaultIfEmpty(
                                                                Product.builder() // si para el id indicado no existe crea un mono empty
                                                                       .build());
        model.addAttribute("title", "Editar Producto");
        model.addAttribute("product", productMono);

        return Mono.just("form");
    }

    @GetMapping("form-v2/{id}")
    public Mono<String> editV2(@PathVariable final String id,
                               final Model model)
    {
        // en este caso el session attribute no se va a guardar ya que los atributos añadidos al model
        // estan dentro del contexto del stream y aqui si sería obligatorio usar el input hidden.
        return productService.findById(id)
                             .doOnNext(prod -> {
                                 LOG.info("Producto: {}", prod.getName());
                                 model.addAttribute("title", "Editar Producto");
                                 model.addAttribute("product", prod);
                                 model.addAttribute("button", "Editar");
                             })
                             .defaultIfEmpty(Product.builder() // si para el id indicado no existe crea un mono empty
                                                    .build())
                             .flatMap(prod -> {
                                 if (prod.getId() == null) {
                                     return Mono.error(new InterruptedException("No existe el producto"));
                                 }
                                 return Mono.just(prod);
                             })
                             .thenReturn("form")
                             //.then(Mono.just("form"))
                             .onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+el+producto"));
    }


    /**
     * @param product El producto del formulario gracias al session atribute tiene el id, siempre y cuando usemos la version1 del edit. Otra particularidad es que no es necesario colocar ModelAttribute (no funciono en pruebas) si el nombre del atributo enviado desde el formulario es el mismo que la clase (product - Product)
     * @param result
     * @param model
     * @param status
     * @return
     */
    @PostMapping("/form")
    public Mono<String> guardar(@Valid final Product product,
                                final BindingResult result,
                                //siempre pegado al objecto a validar
                                final Model model,
                                @RequestPart final FilePart filee,
                                final SessionStatus status)
    {
        if (result.hasErrors()) {
            model.addAttribute("title", "Errores en formulario Producto");
            // con la version 1 del edit no es necesario porque pasa desde el session attribute
            model.addAttribute("product", product);
            model.addAttribute("button", "Guardar");
            return Mono.just("form");
        } else {
            // clean the product session attribute from the session.
            status.setComplete();

            final Mono<Category> category = productService.findCatById(product.getCategory()
                                                                              .getId());
            return category.flatMap(cat -> {
                if (product.getCreateAt() == null) {
                    product.setCreateAt(LocalDate.now());
                }
                if (!filee.filename().isEmpty()) {
                    product.setPhoto(UUID.randomUUID().toString() + "_" +
                                     filee.filename().replace(" ", "")
                                          .replace(":", "")
                                          .replace("\\", ""));
                }
                product.setCategory(cat);
                return productService.save(product);
            }).doOnNext(prod -> {
                LOG.info("Producto guardado: {} , Id: {}", prod.getName(), prod.getId());
            }).flatMap(prod -> {
                if (!filee.filename().isEmpty()) {
                    return filee.transferTo(new File(prod.getPhoto()));
                }
                return Mono.empty();
            }).thenReturn("redirect:/list?success=producto+guardado+con+exito"); // redirige a otra ruta
            //.then(Mono.just("redirect:/list")) // otra alternativa a lo de arriba
        }
    }

    @GetMapping("/delete/{id}")
    public Mono<String> delete(@PathVariable final String id)
    {
        return productService.findById(id)
                             .defaultIfEmpty(Product.builder() // si para el id indicado no existe crea un mono empty
                                                    .build())
                             .flatMap(prod -> {
                                 if (prod.getId() == null) {
                                     return Mono.error(new InterruptedException("No existe el producto a eliminar"));
                                 }
                                 return Mono.just(prod);
                             })
                             .flatMap(prod -> {
                                 LOG.info("Eliminando producto: {}", prod.getName());
                                 return productService.delete(prod);
                             })
                             .thenReturn("redirect:/list?success=producto+eliminado+con+exito")
                             .onErrorResume(ex -> Mono.just("redirect:/list?error=no+existe+el+producto+a+eliminar"));
    }

    @GetMapping("/list-datadrive")
    public String listDataDrive(final Model model)
    {
        final Flux<Product> products = productService.findAllWithNameUpperCase()
                                                     .delayElements(Duration.ofSeconds(1));
        // aqui tenemos otro observador (el log)
        products.subscribe(prod -> LOG.info(prod.getName()));

        // forma para trabajar el back-pressure (contrapresion) y cargar data en bloques (cantidad de elementos)
        model.addAttribute("products", new ReactiveDataDriverContextVariable(products, 2));
        model.addAttribute("title", "Listado de Productos");
        return "list";
    }

    @GetMapping("/list-full")
    public String listFull(final Model model)
    {
        final Flux<Product> products = productService.findAllWithNameUpperCaseRepeat();

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        return "list";
    }

    @GetMapping("/list-chunked")
    public String listChunked(final Model model)
    {
        final Flux<Product> products = productService.findAllWithNameUpperCaseRepeat();

        model.addAttribute("products", products);
        model.addAttribute("title", "Listado de Productos");
        return "list-chunked";
    }
}
