package com.beanandbrew.controller;

import com.beanandbrew.dto.AdminOverviewResponse;
import com.beanandbrew.dto.StatusUpdateRequest;
import com.beanandbrew.entity.Order;
import com.beanandbrew.entity.Product;
import com.beanandbrew.service.AdminService;
import com.beanandbrew.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.beanandbrew.dto.InventoryItem;
import com.beanandbrew.dto.StockUpdateRequest;
import com.beanandbrew.dto.ReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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

    @GetMapping("/orders")
    public List<Order> allOrders() {
        return adminService.getAllOrders();
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateOrderStatus(id, request.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/inventory")
    public List<InventoryItem> inventory() {
        return adminService.getInventory();
    }

    @PutMapping("/inventory/stock")
    public ResponseEntity<?> updateStock(@RequestBody StockUpdateRequest request) {
        try {
            return ResponseEntity.ok(adminService.updateStock(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/reports")
    public ResponseEntity<?> report(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            ReportResponse report = adminService.getReport(from, to);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/reports/orders.csv")
    public ResponseEntity<?> ordersCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            String csv = adminService.buildOrdersCsv(from, to);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"orders.csv\"")
                    .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                    .body(csv.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}