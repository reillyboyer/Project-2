package edu.canisius.csc213.complaints.service;

import edu.canisius.csc213.complaints.model.Complaint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Computes cosine similarity between complaint embeddings and returns
 * the three most similar complaints to a given target.
 */
public class ComplaintSimilarityService {

    private final List<Complaint> complaints;

    public ComplaintSimilarityService(List<Complaint> complaints) {
        this.complaints = complaints;
    }

    /**
     * Return the three complaints whose embeddings have the highest cosine
     * similarity to {@code target}.  The target itself is excluded.
     */
    public List<Complaint> findTop3Similar(Complaint target) {
        if (target == null || target.getEmbedding() == null) {
            throw new IllegalArgumentException("Target complaint must have a non-null embedding");
        }

        double[] targetVec = target.getEmbedding();
        List<ComplaintWithScore> scored = new ArrayList<>();

        for (Complaint c : complaints) {
            if (c == null || c.getComplaintId() == target.getComplaintId()) {
                continue;                          // skip itself
            }
            double[] vec = c.getEmbedding();
            if (vec == null || vec.length != targetVec.length) {
                continue;                          // cannot compare
            }
            double score = cosineSimilarity(targetVec, vec);
            scored.add(new ComplaintWithScore(c, score));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble((ComplaintWithScore cw) -> cw.score).reversed())
                .limit(3)
                .map(cw -> cw.complaint)
                .collect(Collectors.toList());
    }

    /**
     * Cosine similarity in the closed interval [-1, 1].
     */
    private double cosineSimilarity(double[] a, double[] b) {
        double dot = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            magA += a[i] * a[i];
            magB += b[i] * b[i];
        }

        if (magA == 0 || magB == 0) {
            return 0.0;                            // undefined → treat as no similarity
        }
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    /** Helper record for sorting by similarity score */
    private static class ComplaintWithScore {
        final Complaint complaint;
        final double score;
        ComplaintWithScore(Complaint c, double s) {
            this.complaint = Objects.requireNonNull(c);
            this.score = s;
        }
    }
}
