package com.contextual.engine.adsimulator.repository;

import com.contextual.engine.adsimulator.model.AdCreative;
import com.contextual.engine.adsimulator.model.Category;
import org.springframework.data.jpa.repository.JpaRepository; // Correct import
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
// The corrected line is below: JpaRepository, not JpßaRepository
public interface AdCreativeRepository extends JpaRepository<AdCreative, Long> {
     List<AdCreative> findByCategory(Category category);
}