package com.example.stockalert.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "SKU is required")
    @Column(nullable = false, unique = true)
    private String sku;

    @NotNull(message = "Reorder threshold is required")
    @PositiveOrZero(message = "Reorder threshold cannot be negative")
    @Column(nullable = false)
    private Integer reorderThreshold;

    @NotNull(message = "Reorder quantity is required")
    @PositiveOrZero(message = "Reorder quantity cannot be negative")
    @Column(nullable = false)
    private Integer reorderQuantity;

    public Product() {
    }

    public Product(String name, String sku, Integer reorderThreshold, Integer reorderQuantity) {
        this.name = name;
        this.sku = sku;
        this.reorderThreshold = reorderThreshold;
        this.reorderQuantity = reorderQuantity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }
}
