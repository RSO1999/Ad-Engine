package com.contextual.engine.adsimulator.repository;

import com.contextual.engine.adsimulator.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}