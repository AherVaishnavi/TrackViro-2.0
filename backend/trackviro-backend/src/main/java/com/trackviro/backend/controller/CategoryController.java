package com.trackviro.backend.controller;

import com.trackviro.backend.dto.category.CategoryRequest;
import com.trackviro.backend.dto.category.CategoryResponse;
import com.trackviro.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Replaces the category-related endpoints of
 * com.example.demo.controller.FinanceController
 * ("/finance/category", "/finance/category/save"). Create + list
 * only, matching the old app's actual scope — no update/delete
 * existed there either.
 */
@RestController
@RequestMapping("/api/finance/categories")
@Tag(name = "Finance", description = "Requires role FINANCE. Global (unrestricted) final approval, reimbursement, analytics, dashboard.")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(201).body(categoryService.save(request));
    }
}
