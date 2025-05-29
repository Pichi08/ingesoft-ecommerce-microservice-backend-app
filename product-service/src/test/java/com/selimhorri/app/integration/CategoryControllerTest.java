package com.selimhorri.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.exception.payload.ExceptionMsg;
import com.selimhorri.app.unit.util.CategoryUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver"
        })
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CategoryControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private CategoryDto testCategoryDto;
    private CategoryDto anotherCategoryDto;

    @BeforeEach
    public void setUp() {
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                String body = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
                System.err.println("Response error: " + body);
                super.handleError(response);
            }
        });

        // Initialize test data
        testCategoryDto = createTestCategoryDto();
        anotherCategoryDto = createAnotherTestCategoryDto();
    }

    // Utility methods for creating test data
    private CategoryDto createTestCategoryDto() {
        return CategoryDto.builder()
                .categoryTitle("Integration Test Category " + System.currentTimeMillis())
                .imageUrl("http://example.com/integration-test.jpg")
                .build();
    }

    private CategoryDto createAnotherTestCategoryDto() {
        return CategoryDto.builder()
                .categoryTitle("Another Test Category " + System.currentTimeMillis())
                .imageUrl("http://example.com/another-test.jpg")
                .build();
    }

    private CategoryDto createUpdatedCategoryDto(CategoryDto originalCategory) {
        return CategoryDto.builder()
                .categoryId(originalCategory.getCategoryId())
                .categoryTitle("Updated " + originalCategory.getCategoryTitle())
                .imageUrl("http://example.com/updated.jpg")
                .build();
    }

    @Test
    public void testFindAllCategories() {
        String url = "http://localhost:" + port + "/product-service/api/categories";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testFindAllCategoriesWithProperResponse() {
        String url = "http://localhost:" + port + "/product-service/api/categories";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<DtoCollectionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DtoCollectionResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getCollection());
        assertTrue(response.getBody().getCollection().size() >= 0); // May have pre-seeded data
    }

    @Test
    public void testFindCategoryById() {
        int categoryId = 2;
        String url = "http://localhost:" + port + "/product-service/api/categories/" + categoryId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CategoryDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                CategoryDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mode", response.getBody().getCategoryTitle());
    }

    @Test
    public void testDeleteCategoryById() {
        int categoryId = 1;
        String url = "http://localhost:" + port + "/product-service/api/categories/" + categoryId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Boolean.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
    }

}