package com.trackviro.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.trackviro.backend.model.Category;

/** Ported unchanged from com.example.demo.repository.CategoryRepository. */
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
