package com.beanandbrew.controller;

import com.beanandbrew.dto.AdminOverviewResponse;
import com.beanandbrew.entity.Product;
import com.beanandbrew.service.AdminService;
import com.beanandbrew.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    public AdminController(AdminService adminService, ProductService productService) {
        this.adminService = adminService;
        this.productService = productService;
    }

    @GetMapping("/overview")
    public AdminOverviewResponse overview() {
        return adminService.getOverview();
    }

    @GetMapping("/products")
    public List<Product> allProducts() {
        return productService.getAllForAdmin();
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<?> product(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}