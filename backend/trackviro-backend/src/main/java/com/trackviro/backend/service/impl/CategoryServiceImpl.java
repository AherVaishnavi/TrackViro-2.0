package com.trackviro.backend.service.impl;

import com.trackviro.backend.dto.category.CategoryRequest;
import com.trackviro.backend.dto.category.CategoryResponse;
import com.trackviro.backend.model.Category;
import com.trackviro.backend.repository.CategoryRepository;
import com.trackviro.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** Ported from com.example.demo.service.impl.CategoryServiceImpl, adapted to DTOs. */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired private CategoryRepository categoryRepository;

    @Override
    public CategoryResponse save(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setMaxLimit(request.maxLimit());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }
}
