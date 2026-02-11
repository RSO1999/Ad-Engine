## Contextual Ad Engine – Privacy-First Advertising Platform

A full-stack system that analyzes webpage content and serves relevant ads **without user tracking**. The engine uses a hybrid AI + rule-based taxonomy to classify pages in real time and select ads based solely on semantic relevance.

### Overview

* **Backend:** Spring Boot REST API
* **Frontend:** React + TypeScript
* **Data Layer:** MySQL with Spring Data JPA
* **NLP Pipeline:** Python (spaCy, TF-IDF, K-Means)
* **HTML Processing:** Jsoup
* **Performance:** Spring `@Cacheable` for sub-50ms cached responses

Project Structure
Ad-Simulator/ad-simulator – Spring Boot backend and AnalysisService logic
Ad-Simulator/contextual-ui – React frontend interface
Ad-Simulator/taxonomy-pipeline – Python NLP pipeline and database population scripts

### How It Works

#### Offline Intelligence Pipeline

1. Python NLP job processes 20k+ documents using spaCy preprocessing and TF-IDF.
2. K-Means clusters discover semantic topics.
3. Script writes taxonomy and keyword weights to MySQL.
4. Human curation assigns high-confidence terms and links ads to categories.

#### Runtime Decision Flow

1. User submits a URL from the React app.
2. **Jsoup** fetches and parses HTML; headers and body text are extracted.
3. `AnalysisService.java` removes stop words and scores terms:

   * Title words = higher weight
   * Body words = base weight
4. Terms are matched against the taxonomy in MySQL (cached repository).
5. Final score = **(position weight × taxonomy weight)**.
6. Highest-scoring category selects the ad, returned as JSON to the UI.

### Key Features

* Privacy-first: no cookies or user profiling
* Hybrid semantic + rule-based classification
* Decoupled intelligence layer—taxonomy updates require **no code deploy**
* Caching with Spring for low latency
* Repository pattern with Spring Data JPA

### Results

* Classification accuracy improved from ~40% (pure AI) → **90%+** with hybrid scoring
* New articles: <500ms response
* Cached articles: **<50ms** response

### What JSoup Enables

* Fetching remote pages
* Parsing HTML into a queryable structure
* Extracting targeted elements (`h1`, `title`, `p`)
* Returning clean text for analysis

---

This project demonstrates building a scalable, enterprise-style classification service that separates **data intelligence (Python/MySQL)** from **runtime decision logic (Spring Boot)** to deliver fast, privacy-respecting contextual advertising.
