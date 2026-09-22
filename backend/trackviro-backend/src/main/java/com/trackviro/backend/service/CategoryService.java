package com.trackviro.backend.service;

import com.trackviro.backend.dto.category.CategoryRequest;
import com.trackviro.backend.dto.category.CategoryResponse;

import java.util.List;

/** Ported from com.example.demo.service.CategoryService, adapted to DTOs.
 *  create + list only, matching the old app's actual scope — no
 *  update/delete existed there either. */
public interface CategoryService {
    CategoryResponse save(CategoryRequest request);
    List<CategoryResponse> getAll();
}
