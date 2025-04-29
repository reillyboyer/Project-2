package edu.canisius.csc213.complaints.controller;

import edu.canisius.csc213.complaints.model.Complaint;
import edu.canisius.csc213.complaints.service.ComplaintSimilarityService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ComplaintController {

    private final List<Complaint> complaints;
    private final ComplaintSimilarityService similarityService;
    private final Map<Long, Integer> idToIndex;   

    public ComplaintController(List<Complaint> complaints,
                               ComplaintSimilarityService similarityService) {
        this.complaints = complaints;
        this.similarityService = similarityService;
        this.idToIndex = IntStream.range(0, complaints.size())
                                  .boxed()
                                  .collect(Collectors.toMap(i -> complaints.get(i).getComplaintId(),
                                                            i -> i));
    }

    @GetMapping("/complaint")
    public String showComplaint(@RequestParam(name = "index", defaultValue = "0") String indexParam,
                                Model model) {

        int index; String error = null;

        try {
            index = Integer.parseInt(indexParam);
            if (index < 0 || index >= complaints.size()) throw new IndexOutOfBoundsException();
        } catch (NumberFormatException e) {
            error = "Please enter a whole number between 0 and " + (complaints.size() - 1) + ".";
            index = 0;
        } catch (IndexOutOfBoundsException e) {
            error = "Index must be between 0 and " + (complaints.size() - 1) + ".";
            index = 0;
        }

        Complaint c = complaints.get(index);

        List<Row> similarRows = similarityService.findTop3Similar(c).stream()
                .map(sim -> new Row(idToIndex.get(sim.getComplaintId()), sim))
                .toList();

        model.addAttribute("complaint", c);
        model.addAttribute("index", index);
        model.addAttribute("prevIndex", Math.max(index - 1, 0));
        model.addAttribute("nextIndex", Math.min(index + 1, complaints.size() - 1));
        model.addAttribute("similarRows", similarRows);
        model.addAttribute("error", error);
        return "complaint";
    }

    @GetMapping("/search")
    public String searchByCompany(@RequestParam(name = "company", required = false) String raw,
                                  Model model) {

        if (raw == null || raw.isBlank()) {
            model.addAttribute("error", "Please enter a company name.");
            return "search";
        }

        String needle = normalize(raw);

        List<Row> hits = IntStream.range(0, complaints.size())
                .filter(i -> normalize(complaints.get(i).getCompany()).contains(needle))
                .mapToObj(i -> new Row(i, complaints.get(i)))
                .toList();

        if (hits.isEmpty()) {
            model.addAttribute("error", "No complaints found for that company.");
        } else {
            model.addAttribute("searchResults", hits);
        }
        model.addAttribute("companySearch", raw);
        return "search";
    }

    private static String normalize(String s) {
        if (s == null) return "";
        String tmp = Normalizer.normalize(s, Normalizer.Form.NFD)
                               .replaceAll("\\p{M}", "");      
        tmp = tmp.replaceAll("[^\\p{Alnum}\\s]", " ")        
                 .replaceAll("\\s+", " ")                      
                 .trim()
                 .toLowerCase(Locale.ROOT);
        return tmp;
    }

    public record Row(int index, Complaint complaint) {}
}
