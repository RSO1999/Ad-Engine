package com.contextual.engine.adsimulator.service;

import com.contextual.engine.adsimulator.dto.AnalysisRequest;
import com.contextual.engine.adsimulator.dto.AnalysisResponse;
import com.contextual.engine.adsimulator.model.AdCreative;
import com.contextual.engine.adsimulator.model.Category;
import com.contextual.engine.adsimulator.model.Keyword;
import com.contextual.engine.adsimulator.repository.AdCreativeRepository;
import com.contextual.engine.adsimulator.repository.KeywordRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    // Scoring weights for our semantic analysis
    private static final int TITLE_KEYWORD_SCORE = 3;
    private static final int BODY_KEYWORD_SCORE = 1;

    private final AdCreativeRepository adCreativeRepository;
    private final KeywordRepository keywordRepository;
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    // STOP WORDS

    // Removes statistical noise

    /**
     * A predefined set of "stop words" to be excluded from keyword analysis.
     * 
     * WHY THIS IS NECESSARY FOR THE AI:
     * This list acts as a "noise reduction filter" and is a critical pre-processing
     * step
     * for any NLP (Natural Language Processing) system. The AI's job is to find
     * statistically
     * important keywords that define a document's topic. Common words like "the",
     * "a", "is",
     * and "in" are the most frequent words in the English language, but they
     * provide zero
     * contextual meaning.
     * 
     * HOW IT WORKS WITH THE AI:
     * By removing these high-frequency, low-value words BEFORE the analysis begins,
     * we
     * dramatically improve the "signal-to-noise ratio." This allows the AI (in our
     * case,
     * the weighted scoring algorithm) to focus only on the words that are actually
     * meaningful
     * and descriptive of the content, leading to a much more accurate topic
     * classification.
     * It also significantly improves the performance of the analysis.
     */

    private static final Set<String> STOP_WORDS = Set.of("a", "about", "above", "after", "again", "against", "all",
            "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between",
            "both", "but", "by", "can't", "cannot", "could", "did", "do", "does", "doing", "down", "during", "each",
            "few", "for", "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself",
            "him", "himself", "his", "how", "if", "in", "into", "is", "it", "its", "itself", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "our", "ours", "ourselves",
            "out", "over", "own", "same", "she", "should", "so", "some", "such", "than", "that", "the", "their",
            "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those", "through", "to", "too",
            "under", "until", "up", "very", "was", "we", "were", "what", "when", "where", "which", "while", "who",
            "whom", "why", "with", "would", "you", "your", "yours", "yourself", "yourselves", "also", "edit", "see",
            "external", "links");

    public AnalysisService(AdCreativeRepository adCreativeRepository, KeywordRepository keywordRepository) {
        this.adCreativeRepository = adCreativeRepository;
        this.keywordRepository = keywordRepository;
    }

    public Optional<AnalysisResponse> analyze(AnalysisRequest request) {
        try {
            Document doc = Jsoup.connect(request.getUrl()).userAgent(USER_AGENT).get();

            Map<String, Integer> weightedKeywords = extractWeightedKeywords(doc);
            if (weightedKeywords.isEmpty())
                return Optional.empty();

            List<Keyword> foundKeywords = findKeywordsFromDb(new ArrayList<>(weightedKeywords.keySet()));
            if (foundKeywords.isEmpty())
                return Optional.empty();

            // This now returns a Map<Category, Double>
            Map<Category, Double> categoryScores = scoreCategories(foundKeywords, weightedKeywords);
            if (categoryScores.isEmpty())
                return Optional.empty();

            // The rest of the logic works perfectly with the new Double scores
            Category winningCategory = Collections.max(categoryScores.entrySet(), Map.Entry.comparingByValue())
                    .getKey();
            System.out.println("--- AnalysisService: Dominant Category is '" + winningCategory.getName() + "'. ---");

            List<AdCreative> matchingAds = adCreativeRepository.findByCategory(winningCategory);
            if (matchingAds.isEmpty())
                return Optional.empty();

            List<String> matchedTerms = foundKeywords.stream()
                    .filter(k -> k.getCategory().equals(winningCategory))
                    .map(Keyword::getTerm)
                    .distinct()
                    .collect(Collectors.toList());

            return Optional.of(new AnalysisResponse(winningCategory.getName(), matchedTerms, matchingAds.get(0)));

        } catch (IOException e) {
            System.err.println("--- Scraper: Error connecting to URL " + request.getUrl() + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    private Map<Category, Double> scoreCategories(List<Keyword> foundKeywords, Map<String, Integer> weightedKeywords) {
        Map<Category, Double> categoryScores = new HashMap<>();
        for (Keyword keyword : foundKeywords) {
            // The score from the article analysis (e.g., 3 for a title word)
            double articleScore = weightedKeywords.getOrDefault(keyword.getTerm(), 0);
            // The confidence score from our taxonomy (e.g., 10.0 for a "golden" keyword)
            double taxonomyWeight = keyword.getWeight();

            // The final score is a product of both
            double finalScore = articleScore * taxonomyWeight;

            categoryScores.merge(keyword.getCategory(), finalScore, Double::sum);
        }
        return categoryScores;
    }

    @Cacheable("keywords")
    public List<Keyword> findKeywordsFromDb(List<String> terms) {
        System.out.println("--- DB CALL: Querying repository for " + terms.size() + " terms. ---");
        return keywordRepository.findByTermIn(terms);
    }

    private Map<String, Integer> extractWeightedKeywords(Document doc) {
        String titleText = doc.select("h1, h2, title").text();
        Set<String> titleKeywords = parseTextToKeywords(titleText);

        String bodyText = doc.select("p, article, .post-body").text();
        Set<String> bodyKeywords = parseTextToKeywords(bodyText);

        Map<String, Integer> weightedKeywords = new HashMap<>();
        bodyKeywords.forEach(kw -> weightedKeywords.put(kw, BODY_KEYWORD_SCORE));
        titleKeywords.forEach(kw -> weightedKeywords.put(kw, TITLE_KEYWORD_SCORE));

        System.out.println("--- AnalysisService: Found " + titleKeywords.size() + " title keywords and "
                + bodyKeywords.size() + " body keywords. ---");
        return weightedKeywords;
    }

    private Set<String> parseTextToKeywords(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");
        return Arrays.stream(words)
                .filter(word -> word.length() > 2 && !STOP_WORDS.contains(word))
                .collect(Collectors.toSet());
    }
}