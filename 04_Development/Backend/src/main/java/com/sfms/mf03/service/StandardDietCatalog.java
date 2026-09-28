package com.sfms.mf03.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * BR-220 (chốt 2026-08-02, Q1) — danh mục chế độ ăn chuẩn là <b>CẤU HÌNH</b>, KHÔNG hardcode
 * trong Java. 5 giá trị mặc định (Halal/Vegetarian/Gluten-Free/Nut-Free/Lactose-Free) khớp
 * {@code mock-data.ts:2862 STANDARD_DIETS}; ghi đè qua {@code sfms.mf03.standard-diets} trong
 * {@code application.yml}.
 */
@Component
@ConfigurationProperties(prefix = "sfms.mf03")
public class StandardDietCatalog {

    private List<String> standardDiets = List.of(
            "Halal", "Vegetarian", "Gluten-Free", "Nut-Free", "Lactose-Free");

    public List<String> getStandardDiets() {
        return standardDiets;
    }

    public void setStandardDiets(List<String> standardDiets) {
        this.standardDiets = standardDiets;
    }

    public boolean isStandard(String dietaryType) {
        return dietaryType != null && standardDiets.stream().anyMatch(d -> d.equalsIgnoreCase(dietaryType));
    }
}
