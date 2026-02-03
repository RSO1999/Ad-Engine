package com.contextual.engine.adsimulator.repository;

import com.contextual.engine.adsimulator.model.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    List<Keyword> findByTermIn(List<String> terms);
}