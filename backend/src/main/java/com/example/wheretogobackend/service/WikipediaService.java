package com.example.wheretogobackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WikipediaService {

    private final WebClient webClient;

    // Простейший кэш в памяти, чтобы не долбить Википедию повторно для уже проверенных мест
    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    public Map<String, String> getWikiData(String wikiTag) {
        if (wikiTag == null || wikiTag.isBlank()) {
            return new HashMap<>();
        }

        // Если уже запрашивали — отдаем из кэша мгновенно без сетевых запросов
        if (cache.containsKey(wikiTag)) {
            return cache.get(wikiTag);
        }

        Map<String, String> result = new HashMap<>();
        try {
            String lang = "ru";
            String title = wikiTag;

            if (wikiTag.contains(":")) {
                String[] parts = wikiTag.split(":", 2);
                lang = parts[0];
                title = parts[1];
            }

            // 1. Очищаем имя от неразрывных пробелов (\u00A0) и скрытых символов
            String cleanTitle = title.replace("\u00A0", " ")
                    .replace("\u200B", "")
                    .trim();

            // 2. Пробуем прямое получение статьи
            String formattedTitle = cleanTitle.replace(" ", "_");
            result = fetchSummary(lang, formattedTitle);

            // 3. ФОЛЛБЭК: Если статья не найдена напрямую, делаем ПОИСК
            if (result.isEmpty()) {
                // ПАУЗА перед поисковым запросом, чтобы не схватить 429 Too Many Requests
                Thread.sleep(350);

                String searchTitle = searchArticleTitle(lang, cleanTitle);
                if (searchTitle != null) {
                    result = fetchSummary(lang, searchTitle);
                }
            }

            // Сохраняем в кэш результат (даже если он пустой, чтобы повторно не стучаться)
            cache.put(wikiTag, result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Поток был прерван при запросе Wikipedia: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка загрузки Wikipedia для " + wikiTag + ": " + e.getMessage());
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
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

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