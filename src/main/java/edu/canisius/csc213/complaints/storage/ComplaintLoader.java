package edu.canisius.csc213.complaints.storage;

import com.opencsv.bean.CsvToBeanBuilder;
import edu.canisius.csc213.complaints.model.Complaint;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * High-level loader that turns a CSV file and a JSON(L) file into a fully
 * populated list of Complaint objects.
 */
public final class ComplaintLoader {

    private ComplaintLoader() {
        /* static helper – no instances */
    }

    /**
     * Load a CSV and a companion embeddings file (either JSON-Lines or one big
     * JSON array) that live on the classpath, merge them, and return the list.
     *
     * @param csvResourcePath  classpath path such as "/complaints_sample_1_30.csv"
     * @param embResourcePath  classpath path such as "/embeddings_sample_1_30.jsonl"
     */
    public static List<Complaint> loadComplaintsWithEmbeddings(String csvResourcePath,
                                                               String embResourcePath)
            throws IOException {

        try (InputStream csvStream = ComplaintLoader.class.getResourceAsStream(csvResourcePath);
             InputStream embStream = ComplaintLoader.class.getResourceAsStream(embResourcePath)) {

            if (csvStream == null) {
                throw new FileNotFoundException("CSV resource not found: " + csvResourcePath);
            }
            if (embStream == null) {
                throw new FileNotFoundException("Embeddings resource not found: " + embResourcePath);
            }

            /* ---------- read all complaints from the CSV ---------- */
            List<Complaint> complaints = new CsvToBeanBuilder<Complaint>(
                    new InputStreamReader(csvStream, StandardCharsets.UTF_8))
                    .withType(Complaint.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            /* ---------- read the vectors and attach them ---------- */
            Map<Long, double[]> embeddings = EmbeddingLoader.loadEmbeddings(embStream);
            ComplaintMerger.mergeEmbeddings(complaints, embeddings);

            return complaints;
        }
    }
}

