// EmbeddingLoader.java
package edu.canisius.csc213.complaints.storage;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EmbeddingLoader {

    /**
     * Read a file that contains **either**
     *   • one JSON object after another (usual *.jsonl export) **or**
     *   • a single JSON array containing those objects.
     */
    public static Map<Long,double[]> loadEmbeddings(InputStream in) throws IOException {
        Map<Long,double[]> map = new HashMap<>();
        ObjectMapper om = new ObjectMapper();
        JsonFactory f = om.getFactory();

        try (JsonParser jp = f.createParser(in)) {
            /* move to first meaningful token */
            JsonToken t = jp.nextToken();
            if (t == JsonToken.START_ARRAY) {          //  ── case ①: single big array
                t = jp.nextToken();                    //  advance to first element
            }
            while (t != null && t != JsonToken.END_ARRAY) {
                /* jp is positioned at START_OBJECT */
                JsonNode node = om.readTree(jp);       //  read full object
                long id = extractId(node);             //  "id" or "complaintId"
                if (id != -1 && node.has("embedding")) {
                    double[] vec = om.convertValue(node.get("embedding"), double[].class);
                    map.put(id, vec);
                }
                t = jp.nextToken();                    //  move to next object / array-end / EOF
            }
        }
        return map;
    }

    private static long extractId(JsonNode n) {
        JsonNode idNode = n.has("complaintId") ? n.get("complaintId") : n.get("id");
        if (idNode == null) return -1;
        return idNode.isTextual() ? Long.parseLong(idNode.asText()) : idNode.asLong();
    }
}

