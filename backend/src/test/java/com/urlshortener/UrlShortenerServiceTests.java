package com.urlshortener;

import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.model.ShortenUrlResponse;
import com.urlshortener.repository.ShortenedUrlRepository;
import com.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

public class UrlShortenerServiceTests {

    private static final String BASE_URL = "http://localhost:8080";
    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        var dataSource = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        var jdbcTemplate = new JdbcTemplate(dataSource);
        var repository = new ShortenedUrlRepository(jdbcTemplate);
        repository.init();
        service = new UrlShortenerService(repository);
    }

    @Test
    void shorten_WithValidUrl_ReturnsShortUrlResponse() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/test");

        ShortenUrlResponse response = service.shorten(request, BASE_URL);

        assertNotNull(response.getAlias());
        assertEquals("https://example.com/test", response.getFullUrl());
        assertTrue(response.getShortUrl().startsWith(BASE_URL + "/"));
    }

    @Test
    void shorten_WithCustomAlias_ReturnsCustomAlias() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("my-alias");

        ShortenUrlResponse response = service.shorten(request, BASE_URL);

        assertEquals("my-alias", response.getAlias());
        assertEquals(BASE_URL + "/my-alias", response.getShortUrl());
    }

    @Test
    void getFullUrl_ReturnsExistingUrl() throws ExecutionException, InterruptedException {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/");
        request.setCustomAlias("example");
        service.shorten(request, BASE_URL);

        String fullUrl = CompletableFuture.supplyAsync(() -> service.getFullUrl("example")).get();

        assertEquals("https://example.com/", fullUrl);
    }

    @Test
    void delete_WithExistingAlias_RemovesUrlAndReturnsTrue() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/to-delete");
        request.setCustomAlias("to-delete");
        service.shorten(request, BASE_URL);

        boolean deleted = service.delete("to-delete");

        assertTrue(deleted, "delete should return true for an existing alias");
        assertNull(service.getFullUrl("to-delete"),
                "alias should no longer resolve after deletion");
    }

    @Test
    void delete_WithUnknownAlias_ReturnsFalse() {
        boolean deleted = service.delete("does-not-exist");
        assertFalse(deleted, "delete should return false when the alias does not exist");
    }

    @Test
    void delete_ThenReuseAlias_Succeeds() {
        var first = new ShortenUrlRequest();
        first.setFullUrl("https://example.com/first");
        first.setCustomAlias("reusable");
        service.shorten(first, BASE_URL);

        assertTrue(service.delete("reusable"));

        var second = new ShortenUrlRequest();
        second.setFullUrl("https://example.com/second");
        second.setCustomAlias("reusable");

        ShortenUrlResponse response = service.shorten(second, BASE_URL);
        assertEquals("reusable", response.getAlias());
        assertEquals("https://example.com/second", service.getFullUrl("reusable"));
    }
}
