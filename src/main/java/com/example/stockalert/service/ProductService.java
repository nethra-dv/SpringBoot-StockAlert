package com.example.stockalert.service;

import com.example.stockalert.dto.ProductRequest;
import com.example.stockalert.dto.ProductResponse;
import com.example.stockalert.entity.Product;
import com.example.stockalert.exception.BusinessRuleException;
import com.example.stockalert.exception.ResourceNotFoundException;
import com.example.stockalert.repository.ProductRepository;
import com.example.stockalert.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public ProductService(
            ProductRepository productRepository,
            StockMovementRepository stockMovementRepository
    ) {
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new BusinessRuleException("A product with SKU '" + request.sku() + "' already exists.");
        }

        Product product = new Product(
                request.name(),
                request.sku(),
                request.reorderThreshold(),
                request.reorderQuantity()
        );

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(findProduct(id));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);

        if (productRepository.existsBySkuAndIdNot(request.sku(), id)) {
            throw new BusinessRuleException("A different product already uses SKU '" + request.sku() + "'.");
        }

        product.setName(request.name());
        product.setSku(request.sku());
        product.setReorderThreshold(request.reorderThreshold());
        product.setReorderQuantity(request.reorderQuantity());

        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findProduct(id);

        if (!stockMovementRepository.findByProductIdOrderByCreatedAtDesc(id).isEmpty()) {
            throw new BusinessRuleException(
                    "Cannot delete a product that already has stock movement history."
            );
        }

        productRepository.delete(product);
    }

    public Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public ProductResponse toResponse(Product product) {
        int currentStock = stockMovementRepository.calculateCurrentStock(product.getId());
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getReorderThreshold(),
                product.getReorderQuantity(),
                currentStock
        );
    }
}
