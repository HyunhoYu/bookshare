package my.domain.book_request.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.domain.book_request.dto.BookSearchResultDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("!test")
@Slf4j
public class NaverBookSearchService implements BookSearchService {

    @Value("${naver.api.client-id:}")
    private String clientId;

    @Value("${naver.api.client-secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<BookSearchResultDto> search(String query) {
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            log.warn("Naver API keys not configured, returning empty results");
            return List.of();
        }

        URI uri = UriComponentsBuilder
                .fromUriString("https://openapi.naver.com/v1/search/book.json")
                .queryParam("query", query)
                .queryParam("display", 20)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return parseNaverResponse(response.getBody());
        } catch (Exception e) {
            log.error("Naver book search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<BookSearchResultDto> parseNaverResponse(String body) {
        List<BookSearchResultDto> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.get("items");
            if (items == null || !items.isArray()) {
                return results;
            }

            for (JsonNode item : items) {
                String isbn = item.has("isbn") ? item.get("isbn").asText("") : "";
                String title = item.has("title") ? stripHtml(item.get("title").asText("")) : "";
                String author = item.has("author") ? stripHtml(item.get("author").asText("")) : "";
                String publisher = item.has("publisher") ? stripHtml(item.get("publisher").asText("")) : "";
                String thumbnail = item.has("image") ? item.get("image").asText("") : "";
                String description = item.has("description") ? stripHtml(item.get("description").asText("")) : "";

                results.add(new BookSearchResultDto(isbn, title, author, publisher, thumbnail, description));
            }
        } catch (Exception e) {
            log.error("Failed to parse Naver response: {}", e.getMessage());
        }
        return results;
    }

    private String stripHtml(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
