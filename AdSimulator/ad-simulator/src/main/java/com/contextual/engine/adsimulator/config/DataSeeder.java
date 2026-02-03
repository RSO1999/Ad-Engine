package com.contextual.engine.adsimulator.config;

import com.contextual.engine.adsimulator.model.*;
import com.contextual.engine.adsimulator.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

// THIS CLASS IS OBSOLETE AND NO LONGER IN USE
// ONLY USED AS A FAILSAFE IF DATABASE IS DROPPED OR NOT POPULATED

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final KeywordRepository keywordRepository;
    private final AdCreativeRepository adCreativeRepository;

    public DataSeeder(CategoryRepository categoryRepository, KeywordRepository keywordRepository,
            AdCreativeRepository adCreativeRepository) {
        this.categoryRepository = categoryRepository;
        this.keywordRepository = keywordRepository;
        this.adCreativeRepository = adCreativeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() > 0) {
            System.out.println("--- Database already seeded. ---");
            return;
        }
        System.out.println("--- Seeding database with scalable taxonomy ---");

        // 1. Create Categories
        Category sports = categoryRepository.save(new Category("Sports"));
        Category tech = categoryRepository.save(new Category("Technology"));
        Category travel = categoryRepository.save(new Category("Travel"));

        // 2. Create Keywords and associate them with Categories
        keywordRepository.saveAll(List.of(
                new Keyword("sports", sports), new Keyword("game", sports), new Keyword("league", sports),
                new Keyword("team", sports), new Keyword("soccer", sports), new Keyword("running", sports),
                new Keyword("fitness", sports), new Keyword("athletic", sports), new Keyword("nfl", sports),

                new Keyword("tech", tech), new Keyword("technology", tech), new Keyword("apple", tech),
                new Keyword("computer", tech), new Keyword("laptop", tech), new Keyword("software", tech),
                new Keyword("ai", tech),

                new Keyword("travel", travel), new Keyword("vacation", travel), new Keyword("beach", travel),
                new Keyword("resort", travel), new Keyword("hotel", travel), new Keyword("flight", travel)));

        // 3. Create Ads and associate them with Categories
        AdCreative nikeAd = new AdCreative();
        nikeAd.setName("Nike Pegasus Running Shoes");
        nikeAd.setImageUrl(
                "https://static.nike.com/a/images/t_PDP_1280_v1/f_auto,q_auto:eco/i1-e1f20c4c-a3f2-43f2-8c27-4a8e4a7a8a6e/pegasus-40-mens-road-running-shoes-d1lp6w.png");
        nikeAd.setKeywords(List.of("running", "fitness", "shoes"));
        nikeAd.setCategory(sports);

        AdCreative travelAd = new AdCreative();
        travelAd.setName("Sandals All-Inclusive Resort");
        travelAd.setImageUrl("https://www.sandals.com/blog/content/images/2021/03/Sandals-Ochi-Beach-Club.jpg");
        travelAd.setKeywords(List.of("vacation", "beach", "resort"));
        travelAd.setCategory(travel);

        AdCreative techAd = new AdCreative();
        techAd.setName("New M3 MacBook Pro");
        techAd.setImageUrl(
                "https://store.storeimages.cdn-apple.com/1/as-images.apple.com/is/mbp-14-digitalmat-gallery-1-202410?wid=728&hei=666&fmt=png-alpha&.v=dmVFbEEyUXJ6Q0hEd1FjMFY3bE5FczNWK01TMHBhR0pZcm42OHQ2ODBjVVZYRUFzTnU5dXpMeUpXTHdIdkp5VDRob044alBIMUhjRGJwTW1yRE1oUG9oQ20zUjdkYWFQM0VDcG9EZ0J2dDMrNmVjbmk5c1V4VVk2VEt3TGcxekg");
        techAd.setKeywords(List.of("tech", "computer", "laptop"));
        techAd.setCategory(tech);

        adCreativeRepository.saveAll(List.of(nikeAd, travelAd, techAd));
        System.out.println("--- Seeding complete. ---");
    }
}