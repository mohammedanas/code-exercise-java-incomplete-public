package com.urlshortener.service;

import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.repository.ShortenedUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private ShortenedUrlRepository repository;

    @InjectMocks
    private UrlShortenerService service;

    @Test
    void shorten_WithCustomAlias_UsesCustomAlias() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("my-alias");

        when(repository.existsByAlias("my-alias")).thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getAlias()).isEqualTo("my-alias");
        assertThat(response.getShortUrl()).isEqualTo(BASE_URL + "/my-alias");
        assertThat(response.getFullUrl()).isEqualTo("https://example.com/");
        verify(repository).save(eq("my-alias"), eq("https://example.com/"), any());
    }

    @Test
    void shorten_WithCustomAlias_TrimsWhitespace() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("  spaced  ");

        when(repository.existsByAlias("spaced")).thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getAlias()).isEqualTo("spaced");
        verify(repository).save(eq("spaced"), anyString(), any());
    }

    @Test
    void shorten_WithDuplicateAlias_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("taken");

        when(repository.existsByAlias("taken")).thenReturn(true);

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("taken");

        verify(repository, never()).save(anyString(), anyString(), any());
    }

    @Test
    void shorten_WithInvalidAlias_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("bad alias!");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Alias");

        verify(repository, never()).save(anyString(), anyString(), any());
    }

    @Test
    void shorten_WithSingleCharAlias_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("a");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shorten_WithoutCustomAlias_GeneratesUniqueAlias() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/test");

        when(repository.existsByAlias(anyString())).thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getAlias())
                .isNotNull()
                .hasSize(7)
                .matches("[a-zA-Z0-9]{7}");
        assertThat(response.getShortUrl()).isEqualTo(BASE_URL + "/" + response.getAlias());
        assertThat(response.getFullUrl()).isEqualTo("https://example.com/test");
    }

    @Test
    void shorten_WhenGeneratedAliasCollides_RetriesUntilFree() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");

        when(repository.existsByAlias(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getAlias()).hasSize(7);
        verify(repository, atLeast(2)).existsByAlias(anyString());
    }

    @Test
    void shorten_WithBareHost_AddsTrailingSlash() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com");
        request.setCustomAlias("bare");

        when(repository.existsByAlias("bare")).thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getFullUrl()).isEqualTo("https://example.com/");
    }

    @Test
    void shorten_WithQueryAndFragment_PreservesThem() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/path?a=1&b=2#frag");
        request.setCustomAlias("qf");

        when(repository.existsByAlias("qf")).thenReturn(false);

        var response = service.shorten(request, BASE_URL);

        assertThat(response.getFullUrl())
                .isEqualTo("https://example.com/path?a=1&b=2#frag");
    }

    @Test
    void shorten_WithBlankUrl_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("   ");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fullUrl is required");
    }

    @Test
    void shorten_WithNullUrl_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl(null);

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fullUrl is required");
    }

    @Test
    void shorten_WithRelativeUrl_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("/just/a/path");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid URL");
    }

    @Test
    void shorten_WithUnsupportedScheme_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("ftp://example.com/file");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid URL");
    }

    @Test
    void shorten_WithMalformedUrl_Throws() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("http://exa mple.com");

        assertThatThrownBy(() -> service.shorten(request, BASE_URL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("valid URL");
    }

    @Test
    void getFullUrl_ExistingAlias_ReturnsUrl() {
        when(repository.findFullUrlByAlias("abc"))
                .thenReturn(Optional.of("https://example.com/"));

        var result = service.getFullUrl("abc");

        assertThat(result).isEqualTo("https://example.com/");
    }

    @Test
    void getFullUrl_UnknownAlias_ReturnsNull() {
        when(repository.findFullUrlByAlias("missing")).thenReturn(Optional.empty());

        var result = service.getFullUrl("missing");

        assertThat(result).isNull();
    }

    @Test
    void getAll_DelegatesToRepository() {
        var items = java.util.List.of(
                new com.urlshortener.model.UrlListItem(
                        "abc", "https://example.com/", BASE_URL + "/abc"));
        when(repository.findAll(BASE_URL, 20, 0)).thenReturn(items);
        when(repository.count()).thenReturn(1L);

        var result = service.getAll(BASE_URL, 0, 20);  // page=0, size=20

        assertThat(result.items()).isEqualTo(items);
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        verify(repository).findAll(BASE_URL, 20, 0);
    }

    @Test
    void delete_ExistingAlias_ReturnsTrueAndDeletesRow() {
        when(repository.findFullUrlByAlias("abc"))
                .thenReturn(Optional.of("https://example.com/"));

        var deleted = service.delete("abc");

        assertThat(deleted).isTrue();
        verify(repository).deleteByAlias("abc");
    }

    @Test
    void delete_UnknownAlias_ReturnsFalse() {
        when(repository.findFullUrlByAlias("missing")).thenReturn(Optional.empty());

        var deleted = service.delete("missing");

        assertThat(deleted).isFalse();
        verify(repository, never()).deleteByAlias(anyString());
        verify(repository, never()).save(anyString(), anyString(), any());
    }
}