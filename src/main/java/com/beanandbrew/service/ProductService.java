package com.beanandbrew.service;

import com.beanandbrew.dto.ProductRequest;
import com.beanandbrew.dto.VariantRequest;
import com.beanandbrew.entity.Category;
import com.beanandbrew.entity.Product;
import com.beanandbrew.entity.ProductVariant;
import com.beanandbrew.repository.CategoryRepository;
import com.beanandbrew.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        applyRequestToProduct(product, request);
        return productRepository.save(product);
    }

    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        applyRequestToProduct(product, request);
        return productRepository.save(product);
    }

    public void delete(Long id) {
        Product product = getById(id);
        productRepository.delete(product);
    }

    private void applyRequestToProduct(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        product.getVariants().clear();
        if (request.getVariants() != null) {
            List<ProductVariant> variants = new ArrayList<>();
            for (VariantRequest v : request.getVariants()) {
                ProductVariant variant = new ProductVariant();
                variant.setName(v.getName());
                variant.setPrice(v.getPrice());
                variant.setStock(v.getStock());
                variant.setProduct(product);
                variants.add(variant);
            }
            product.getVariants().addAll(variants);
        }
    }
}