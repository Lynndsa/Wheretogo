
package com.example.wheretogobackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

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

        String lang = "ru";
        String title = wikiTag;

        if (wikiTag.contains(":") && !wikiTag.startsWith("http")) {
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

        // Сначала получаем описание и ссылку из Wikipedia
        result = fetchPageData(lang, cleanTitle);

        String commonsSearchTitle = cleanTitle;

        // Если статья не найдена или у неё нет изображения,
        // пробуем найти её название через OpenSearch
        if (!result.containsKey("imageUrl")) {

            String foundTitle = searchArticleTitle(lang, cleanTitle);

            if (foundTitle != null
                    && !foundTitle.equalsIgnoreCase(cleanTitle)) {

                Map<String, String> searchResult =
                        fetchPageData(lang, foundTitle);

                searchResult.forEach(result::putIfAbsent);

                commonsSearchTitle = foundTitle;
            }
        }

        // Сначала ищем фотографию в Wikimedia Commons
        String commonsImage = searchCommonsImage(commonsSearchTitle);

        if (commonsImage != null && !commonsImage.isBlank()) {
            result.put("imageUrl", commonsImage);
        }

        System.out.println(
                "Wikipedia: " + cleanTitle
                        + ", image: " + result.get("imageUrl")
        );

        return result;
    }

    private Map<String, String> fetchPageData(
            String lang,
            String title
    ) {

        Map<String, String> result = new HashMap<>();

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(
                            "https://" + lang + ".wikipedia.org/w/api.php"
                    )
                    .queryParam("action", "query")
                    .queryParam("format", "json")
                    .queryParam("formatversion", "2")
                    .queryParam("redirects", "1")
                    .queryParam("prop", "pageimages|extracts|info")
                    .queryParam("piprop", "thumbnail|original")
                    .queryParam("pithumbsize", "800")
                    .queryParam("exintro", "1")
                    .queryParam("explaintext", "1")
                    .queryParam("inprop", "url")
                    .queryParam("titles", title)
                    .build()
                    .encode()
                    .toUriString();

            JsonNode response = webClient.get()
                    .uri(url)
                    .header(
                            "User-Agent",
                            "WhereToGoBackend/1.0 (ladaromanova83@gmail.com)"
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null
                    || !response.has("query")
                    || !response.get("query").has("pages")) {
                return result;
            }

            JsonNode pages = response
                    .get("query")
                    .get("pages");

            if (!pages.isArray() || pages.isEmpty()) {
                return result;
            }

            JsonNode page = pages.get(0);

            if (page.has("missing")) {
                return result;
            }

            // Описание
            if (page.has("extract")
                    && !page.get("extract").asText().isBlank()) {

                result.put(
                        "description",
                        page.get("extract").asText()
                );
            }

            // Оригинальное изображение
            if (page.has("original")
                    && page.get("original").has("source")) {

                result.put(
                        "imageUrl",
                        page.get("original")
                                .get("source")
                                .asText()
                );
            }

            // Если оригинала нет — миниатюра
            if (!result.containsKey("imageUrl")
                    && page.has("thumbnail")
                    && page.get("thumbnail").has("source")) {

                result.put(
                        "imageUrl",
                        page.get("thumbnail")
                                .get("source")
                                .asText()
                );
            }

            // Ссылка на статью
            if (page.has("fullurl")) {

                result.put(
                        "wikiUrl",
                        page.get("fullurl").asText()
                );
            }

        } catch (Exception e) {
            System.err.println(
                    "Ошибка получения данных Wikipedia для "
                            + title + ": " + e.getMessage()
            );
        }

        return result;
    }

    private String searchArticleTitle(
            String lang,
            String query
    ) {

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(
                            "https://" + lang + ".wikipedia.org/w/api.php"
                    )
                    .queryParam("action", "opensearch")
                    .queryParam("search", query)
                    .queryParam("limit", "1")
                    .queryParam("namespace", "0")
                    .queryParam("format", "json")
                    .build()
                    .encode()
                    .toUriString();

            JsonNode response = webClient.get()
                    .uri(url)
                    .header(
                            "User-Agent",
                            "WhereToGoBackend/1.0 (ladaromanova83@gmail.com)"
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null
                    && response.isArray()
                    && response.size() > 1) {

                JsonNode titles = response.get(1);

                if (titles.isArray() && !titles.isEmpty()) {
                    return titles.get(0).asText();
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "Ошибка поиска статьи Wikipedia: "
                            + e.getMessage()
            );
        }

        return null;
    }

    // Поиск фотографии в Wikimedia Commons
    private String searchCommonsImage(String title) {

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(
                            "https://commons.wikimedia.org/w/api.php"
                    )
                    .queryParam("action", "query")
                    .queryParam("format", "json")
                    .queryParam("formatversion", "2")
                    .queryParam("generator", "search")
                    .queryParam("gsrsearch", title)
                    .queryParam("gsrnamespace", "6")
                    .queryParam("gsrlimit", "10")
                    .queryParam("prop", "imageinfo")
                    .queryParam("iiprop", "url|mime")
                    .queryParam("iiurlwidth", "800")
                    .build()
                    .encode()
                    .toUriString();

            JsonNode response = webClient.get()
                    .uri(url)
                    .header(
                            "User-Agent",
                            "WhereToGoBackend/1.0 (contact@example.com)"
                    )
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null
                    || !response.has("query")
                    || !response.get("query").has("pages")) {
                return null;
            }

            JsonNode pages = response
                    .get("query")
                    .get("pages");

            if (!pages.isArray() || pages.isEmpty()) {
                return null;
            }

            // Перебираем результаты поиска
            for (JsonNode page : pages) {

                String fileTitle = page.path("title").asText("")
                        .toLowerCase();

                // Пропускаем очевидно неподходящие файлы
                if (fileTitle.contains("портрет")
                        || fileTitle.contains("portrait")
                        || fileTitle.contains("картина")
                        || fileTitle.contains("painting")
                        || fileTitle.contains("logo")
                        || fileTitle.contains("логотип")
                        || fileTitle.contains("карта")
                        || fileTitle.contains("map")) {
                    continue;
                }

                JsonNode imageInfo = page
                        .path("imageinfo")
                        .path(0);

                if (imageInfo.isMissingNode()) {
                    continue;
                }

                // Для приложения берём миниатюру шириной 800 px
                String imageUrl = imageInfo
                        .path("thumburl")
                        .asText("");

                // Если миниатюры нет, используем оригинал
                if (imageUrl.isBlank()) {
                    imageUrl = imageInfo
                            .path("url")
                            .asText("");
                }

                if (!imageUrl.isBlank()) {

                    System.out.println(
                            "Wikimedia Commons: "
                                    + page.path("title").asText()
                                    + ", image: " + imageUrl
                    );

                    return imageUrl;
                }
            }

        } catch (Exception e) {
            System.err.println(
                    "Ошибка поиска изображения Wikimedia Commons для "
                            + title + ": " + e.getMessage()
            );
        }

        return null;
    }
}
