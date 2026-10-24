package com.example.wheretogobackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;



@Service
@RequiredArgsConstructor
public class WikipediaService {

    private final WebClient webClient;



    public Map<String, String> getWikiData(String wikiTag) {
        Map<String, String> result = new HashMap<>();

        if (wikiTag == null || wikiTag.isBlank()) {
            return result;
        }

        try {
            String lang = "ru";
            String title = wikiTag;

            if (wikiTag.contains(":")) {
                String[] parts = wikiTag.split(":", 2);
                lang = parts[0];
                title = parts[1];
            }

            String cleanTitle = title
                    .replace("\u00A0", " ")
                    .replace("\u200B", "")
                    .trim();

            if (cleanTitle.isEmpty()) {
                return result;
            }

            String formattedTitle = cleanTitle.replace(" ", "_");

            result = fetchSummary(lang, formattedTitle);

        } catch (Exception e) {
            System.err.println(
                    "Ошибка загрузки Wikipedia для "
                            + wikiTag + ": " + e.getMessage()
            );
        }

        return result;
    }


    private Map<String, String> fetchSummary(String lang, String pageTitle) {
        Map<String, String> result = new HashMap<>();
        try {
            String url = String.format("https://%s.wikipedia.org/api/rest_v1/page/summary/%s", lang, pageTitle);
            JsonNode response = webClient.get()
                    .uri(url)
                    .header("User-Agent", "WhereToGoBackend/1.0 (contact@example.com)")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("extract")) {
                result.put("description", response.get("extract").asText());
                if (response.has("thumbnail") && response.get("thumbnail").has("source")) {
                    result.put("imageUrl", response.get("thumbnail").get("source").asText());
                }
                result.put("wikiUrl", String.format("https://%s.wikipedia.org/wiki/%s", lang, pageTitle));
            }
        } catch (Exception ignored) {

        }
        return result;
    }

    private String searchArticleTitle(String lang, String query) {
        try {
            // Очищаем запрос от слов "музей", "памятник", если они мешают, или ищем строгим opensearch
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            // Используем opensearch — он ищет ТОЛЬКО по заголовкам статей, а не по всему тексту
            String searchUrl = String.format(
                    "https://%s.wikipedia.org/w/api.php?action=opensearch&search=%s&limit=1&namespace=0&format=json",
                    lang, encodedQuery
            );

            JsonNode response = webClient.get()
                    .uri(searchUrl)
                    .header("User-Agent", "WhereToGoBackend/1.0 (contact@example.com)")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            // Opensearch возвращает массив вида: ["запрос", ["Заголовок1"], ["описание"], ["ссылка"]]
            if (response != null && response.isArray() && response.size() > 1) {
                JsonNode titles = response.get(1);
                if (titles.isArray() && titles.size() > 0) {
                    return titles.get(0).asText().replace(" ", "_");
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка поиска в Search API: " + e.getMessage());
        }
        return null;
    }
}