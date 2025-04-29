package edu.canisius.csc213.complaints.storage;

import edu.canisius.csc213.complaints.model.Complaint;

import java.util.List;
import java.util.Map;

/**
 * Utility that attaches embeddings (vectors) to their matching Complaint objects.
 */
public final class ComplaintMerger {

    private ComplaintMerger() {
        /* static helper – no instances */
    }

    /**
     * For every complaint in {@code complaints} look up its vector in {@code embeddings}
     * and copy it into the object.  If a vector is missing we leave the complaint unchanged
     * so that callers can decide how to handle the gap (unit tests expect no gaps).
     */
    public static void mergeEmbeddings(List<Complaint> complaints,
                                       Map<Long, double[]> embeddings) {

        if (complaints == null || embeddings == null) {
            return;                        // nothing to do
        }

        for (Complaint c : complaints) {
            double[] vec = embeddings.get(c.getComplaintId());
            if (vec != null) {
                c.setEmbedding(vec);
            }
        }
    }
}

