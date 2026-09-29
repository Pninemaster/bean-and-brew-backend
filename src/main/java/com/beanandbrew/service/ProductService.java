package com.beanandbrew.service;

import com.beanandbrew.dto.ProductRequest;
import com.beanandbrew.dto.VariantRequest;
import com.beanandbrew.entity.Category;
import com.beanandbrew.entity.Product;
import com.beanandbrew.entity.ProductVariant;
import com.beanandbrew.repository.CategoryRepository;
import com.beanandbrew.repository.OrderItemRepository;
import com.beanandbrew.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                           CategoryRepository categoryRepository,
                           OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    // Storefront: active products only
    public List<Product> getAll() {
        return productRepository.findByActiveTrue();
    }

    // Admin: everything
    public List<Product> getAllForAdmin() {
        return productRepository.findAll();
    }

    // Admin/internal: any product
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
    }

    // Storefront: hides inactive products
    public Product getActiveById(Long id) {
        Product product = getById(id);
        if (!product.isActive()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        return product;
    }

    @Transactional
    public Product create(ProductRequest request) {
        Product product = new Product();
        applyRequestToProduct(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public Product update(Long id, ProductRequest request) {
        Product product = getById(id);
        applyRequestToProduct(product, request);
        return productRepository.save(product);
    }

    @Transactional
    public Product setActive(Long id, boolean active) {
        Product product = getById(id);
        product.setActive(active);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = getById(id);
        if (orderItemRepository.existsByProductId(id)) {
            throw new IllegalArgumentException(
                    "This product appears in past orders and can't be deleted. Deactivate it instead.");
        }
        productRepository.delete(product);
    }

    private void applyRequestToProduct(Product product, ProductRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price must be zero or more");
        }
        int stock = request.getStock() == null ? 0 : request.getStock();
        if (stock < 0) {
            throw new IllegalArgumentException("Stock can't be negative");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        Map<Long, ProductVariant> existingById = new HashMap<>();
        for (ProductVariant existing : product.getVariants()) {
            existingById.put(existing.getId(), existing);
        }

        List<ProductVariant> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (request.getVariants() != null) {
            for (VariantRequest v : request.getVariants()) {
                if (v.getName() == null || v.getName().isBlank()) {
                    throw new IllegalArgumentException("Each size needs a name");
                }
                if (!seen.add(v.getName().trim().toLowerCase())) {
                    throw new IllegalArgumentException("Duplicate size name: " + v.getName().trim());
                }
                if (v.getPrice() == null || v.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Size price must be zero or more");
                }
                int variantStock = v.getStock() == null ? 0 : v.getStock();
                if (variantStock < 0) {
                    throw new IllegalArgumentException("Size stock can't be negative");
                }

                ProductVariant variant = v.getId() != null ? existingById.get(v.getId()) : null;
                if (variant == null) {
                    variant = new ProductVariant();
                    variant.setProduct(product);
                }
                variant.setName(v.getName().trim());
                variant.setPrice(v.getPrice());
                variant.setStock(variantStock);
                result.add(variant);
            }
        }

        product.setName(request.getName().trim());
        product.setPrice(request.getPrice());
        product.setStock(stock);
        product.setImageUrl(request.getImageUrl());
        product.setDescription(request.getDescription());
        product.setCategory(category);

        product.getVariants().retainAll(result);
        for (ProductVariant variant : result) {
            if (!product.getVariants().contains(variant)) {
                product.getVariants().add(variant);
            }
        }
    }
}