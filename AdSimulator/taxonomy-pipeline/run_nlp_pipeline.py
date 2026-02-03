# run_nlp_pipeline.py

import sys
import os
from sklearn.datasets import fetch_20newsgroups
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
import spacy
from tqdm import tqdm
from sqlalchemy import create_engine, text # For connecting to MySQL

# --- Database Configuration ---
# Same credentials as Spring Boot app
DB_USER = "ad_user"
DB_PASS = "root" # MYSQL DB PASSWORD
DB_HOST = "localhost"
DB_PORT = "3306"
DB_NAME = "ad_taxonomy_db"

# --- Data Ingestion ---
print("--- Loading 20 Newsgroups Dataset ---")
try:
    newsgroups_data = fetch_20newsgroups(subset='all', remove=('headers', 'footers', 'quotes'))
    documents = newsgroups_data.data
except Exception as e:
    print(f"Error loading dataset: {e}")
    sys.exit(1)
print(f"Successfully loaded {len(documents)} documents.")
print("--- Loading 20 Newsgroups Dataset Complete ---")


# --- NLP Pre-processing ---
print("\n--- Starting NLP Pre-processing ---")
try:
    nlp = spacy.load("en_core_web_sm", disable=['parser', 'ner'])
    processed_documents = []
    for doc in tqdm(nlp.pipe(documents, batch_size=50), total=len(documents), desc="Pre-processing"):
        tokens = [token.lemma_.lower() for token in doc if not token.is_stop and not token.is_punct and not token.is_space and token.is_alpha]
        processed_documents.append(" ".join(tokens))
except Exception as e:
    print(f"Error during pre-processing: {e}")
    sys.exit(1)
print("--- NLP Pre-processing Complete ---")


# --- Vectorization (TF-IDF) ---
print("\n--- Phase 4: Vectorizing documents with TF-IDF ---")
try:
    vectorizer = TfidfVectorizer(max_df=0.5, min_df=5, ngram_range=(1,2))
    tfidf_matrix = vectorizer.fit_transform(processed_documents)
    feature_names = vectorizer.get_feature_names_out()
    print(f"Shape of the TF-IDF matrix: {tfidf_matrix.shape}")
except Exception as e:
    print(f"Error during vectorization: {e}")
    sys.exit(1)
print("--- Vectorization Complete ---")


# --- Clustering (K-Means) ---
print("\n--- Clustering documents with K-Means ---")
try:
    num_clusters = 20
    kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init='auto')
    print(f"Running K-Means algorithm to find {num_clusters} clusters...")
    kmeans.fit(tfidf_matrix)
    print("Clustering complete.")
except Exception as e:
    print(f"Error during clustering: {e}")
    sys.exit(1)
print("--- Clustering documents with K-Means Complete ---")


# --- Storing to Staging Database ---
print("\n--- Saving discovered topics to MySQL staging tables ---")
try:
    # Creates the database connection string
    db_url = f"mysql+mysqlconnector://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    engine = create_engine(db_url)

    # --- Creates Staging Tables (if they don't exist) ---
    with engine.connect() as connection:
        print("Creating staging tables (if they don't exist)...")
        connection.execute(text("""
            CREATE TABLE IF NOT EXISTS staged_categories (
                id INT AUTO_INCREMENT PRIMARY KEY,
                top_terms_preview VARCHAR(1000) NOT NULL,
                status VARCHAR(50) DEFAULT 'UNREVIEWED' NOT NULL,
                machine_name VARCHAR(255)
            );
        """))
        connection.execute(text("""
            CREATE TABLE IF NOT EXISTS staged_keywords (
                id INT AUTO_INCREMENT PRIMARY KEY,
                term VARCHAR(255) NOT NULL UNIQUE,
                staged_category_id INT,
                FOREIGN KEY (staged_category_id) REFERENCES staged_categories(id)
            );
        """))
        # Clear out old data before inserting new results
        print("Clearing old data from staging tables...")
        connection.execute(text("DELETE FROM staged_keywords;"))
        connection.execute(text("DELETE FROM staged_categories;"))
        connection.commit() # Saves the deletes

    # --- Insert the Discovered Clusters and Keywords ---
    print("Inserting new data into staging tables...")
    order_centroids = kmeans.cluster_centers_.argsort()[:, ::-1]

    with engine.connect() as connection:
        for i in range(num_clusters):
            # Get the top keywords for this cluster
            top_terms = [feature_names[ind] for ind in order_centroids[i, :15]] # Get top 15
            top_terms_preview = ", ".join(top_terms[:10]) # Preview string for the category table
            
            # Create a placeholder name for the category
            machine_name = f"Cluster {i}: {top_terms[0]}, {top_terms[1]}"

            # 1. Insert the category into the staged_categories table
            result = connection.execute(text(
                "INSERT INTO staged_categories (top_terms_preview, machine_name) VALUES (:preview, :name)"
            ), {"preview": top_terms_preview, "name": machine_name})
            
            # Get the ID of the category we just inserted
            category_id = result.lastrowid
            
            # 2. Insert all the keywords for this category into the staged_keywords table
            for term in top_terms:
                connection.execute(text(
                    "INSERT INTO staged_keywords (term, staged_category_id) VALUES (:term, :id) ON DUPLICATE KEY UPDATE term=term"
                ), {"term": term, "id": category_id})
        
        connection.commit() # Save all the inserts

    print(f"Successfully inserted {num_clusters} discovered topics and their keywords into the staging tables.")

except Exception as e:
    print(f"Error during database operation: {e}")
    sys.exit(1)

print("\n--- PIPELINE COMPLETE ---")
