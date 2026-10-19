package com.urlshortener.controller;

import com.urlshortener.model.PagedResult;
import com.urlshortener.model.ShortenUrlRequest;
import com.urlshortener.model.ShortenUrlResponse;
import com.urlshortener.model.UrlListItem;
import com.urlshortener.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlsControllerTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private UrlShortenerService service;

    @Mock
    private HttpServletRequest requestContext;

    @InjectMocks
    private UrlsController controller;

    @Test
    void shorten_ReturnsCreatedWithBody() {
        var request = new ShortenUrlRequest();
        request.setFullUrl("https://example.com/test");

        var expected = new ShortenUrlResponse(
                BASE_URL + "/abc1234", "abc1234", "https://example.com/test");
        when(service.shorten(any(ShortenUrlRequest.class), eq(BASE_URL)))
                .thenReturn(expected);

        stubBaseUrl();

        var response = controller.shorten(request, requestContext);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(service).shorten(request, BASE_URL);
    }

    @Test
    void getAll_UsesDefaultPageAndSize() {
        var paged = new PagedResult<UrlListItem>(java.util.List.of(), 0, 20, 0L);
        when(service.getAll(BASE_URL, 0, 20)).thenReturn(paged);
        stubBaseUrl();

        var result = controller.getAll(0, 20, requestContext);

        assertThat(result).isEqualTo(paged);
        verify(service).getAll(BASE_URL, 0, 20);
    }

    @Test
    void getAll_PassesPageAndSizeThrough() {
        var paged = new PagedResult<UrlListItem>(java.util.List.of(), 3, 50, 0L);
        when(service.getAll(BASE_URL, 3, 50)).thenReturn(paged);
        stubBaseUrl();

        var result = controller.getAll(3, 50, requestContext);

        assertThat(result.page()).isEqualTo(3);
        verify(service).getAll(BASE_URL, 3, 50);
    }

    @Test
    void getAll_CapsOversizedPageSize() {
        var paged = new PagedResult<UrlListItem>(java.util.List.of(), 0, 100, 0L);
        when(service.getAll(BASE_URL, 0, 100)).thenReturn(paged);
        stubBaseUrl();

        controller.getAll(0, 10_000, requestContext);

        // size is capped at 100, not passed through as 10_000
        verify(service).getAll(BASE_URL, 0, 100);
    }

    @Test
    void getAll_NegativePage_Throws() {
        assertThatThrownBy(() -> controller.getAll(-1, 20, requestContext))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(service);
    }

    @Test
    void getAll_ZeroSize_Throws() {
        assertThatThrownBy(() -> controller.getAll(0, 0, requestContext))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(service);
    }


    @Test
    void redirectToUrl_ExistingAlias_Returns308WithLocation() {
        when(service.getFullUrl("abc1234")).thenReturn("https://target.com/");

        var response = controller.redirectToUrl("abc1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PERMANENT_REDIRECT);
        assertThat(response.getHeaders().getLocation()).hasToString("https://target.com/");
    }

    @Test
    void redirectToUrl_UnknownAlias_Returns404() {
        when(service.getFullUrl("missing")).thenReturn(null);

        var response = controller.redirectToUrl("missing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getLocation()).isNull();
    }

    @Test
    void delete_ExistingAlias_Returns204() {
        when(service.delete("abc1234")).thenReturn(true);

        var response = controller.delete("abc1234");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(service).delete("abc1234");
    }

    @Test
    void delete_UnknownAlias_Returns404() {
        when(service.delete("missing")).thenReturn(false);

        var response = controller.delete("missing");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void stubBaseUrl() {
        when(requestContext.getScheme()).thenReturn("http");
        when(requestContext.getServerName()).thenReturn("localhost");
        when(requestContext.getServerPort()).thenReturn(8080);
    }
}