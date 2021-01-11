package com.jcsoft.springbootwebflux.model.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@Document(collation = "product")
public class Product
{
    @Id
    private String id;
    private String name;
    private Double price;
    private LocalDate createAt;

}
