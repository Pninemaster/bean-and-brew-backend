package com.beanandbrew.service;

import com.beanandbrew.dto.CategoryRequest;
import com.beanandbrew.entity.Category;
import com.beanandbrew.repository.CategoryRepository;
import com.beanandbrew.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    public Category create(CategoryRequest request) {
        String name = cleanName(request.getName());
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A category named \"" + name + "\" already exists");
        }
        Category category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    public Category update(Long id, CategoryRequest request) {
        Category category = getById(id);
        String name = cleanName(request.getName());
        if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("A category named \"" + name + "\" already exists");
        }
        category.setName(name);
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = getById(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException(
                    "This category still has products. Move or delete them first.");
        }
        categoryRepository.delete(category);
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return name.trim();
    }
}