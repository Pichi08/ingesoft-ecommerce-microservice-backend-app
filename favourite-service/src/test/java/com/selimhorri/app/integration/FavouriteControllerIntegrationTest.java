package com.selimhorri.app.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;

import java.io.IOException;
import java.nio.charset.Charset;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:testdb",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "eureka.client.enabled=false",
                "spring.cloud.discovery.enabled=false"
        })
@ActiveProfiles("test")
public class FavouriteControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private FavouriteDto favouriteDto;
    private String baseUrl;
    private LocalDateTime likeDate;

    @BeforeEach
    public void setUp() {
        baseUrl = "http://localhost:" + port + "/api/favourites";
        
        // Configure error handling to print response body on error
        restTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                String body = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
                System.err.println("Response error: " + body);
                super.handleError(response);
            }
        });

        // Create test data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT);
        likeDate = LocalDateTime.parse("15-01-2023__10:30:00:000000", formatter);

        // Create favourite DTO
        favouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate)
                .userDto(UserDto.builder()
                        .userId(1)
                        .firstName("John")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .phone("1234567890")
                        .build())
                .productDto(ProductDto.builder()
                        .productId(101)
                        .productTitle("Smartphone")
                        .imageUrl("smartphone.jpg")
                        .sku("SM-12345")
                        .priceUnit(599.99)
                        .quantity(10)
                        .build())
                .build();
    }

    @Test
    @DisplayName("Should create a new favourite")
    public void testCreateFavourite() {
        // Setup
        String url = baseUrl;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<FavouriteDto> requestEntity = new HttpEntity<>(favouriteDto, headers);
        
        // Execute
        ResponseEntity<FavouriteDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                FavouriteDto.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getUserId());
        assertEquals(101, response.getBody().getProductId());
    }

    @Test
    @DisplayName("Should find all favourites")
    public void testFindAllFavourites() {
        // Setup
        String url = baseUrl;
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        // Create a favourite first to ensure there's data
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FavouriteDto> postEntity = new HttpEntity<>(favouriteDto, postHeaders);
        restTemplate.exchange(baseUrl, HttpMethod.POST, postEntity, FavouriteDto.class);
        
        // Execute
        ResponseEntity<Object> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Object.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should find favourite by ID")
    public void testFindFavouriteById() {
        // First create a favourite
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FavouriteDto> postEntity = new HttpEntity<>(favouriteDto, postHeaders);
        restTemplate.exchange(baseUrl, HttpMethod.POST, postEntity, FavouriteDto.class);
        
        // Setup for GET request
        String dateParam = likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT));
        String url = baseUrl + "/1/101/" + dateParam;
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        // Execute
        ResponseEntity<FavouriteDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                FavouriteDto.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getUserId());
        assertEquals(101, response.getBody().getProductId());
    }

    @Test
    @DisplayName("Should delete favourite by ID")
    public void testDeleteFavouriteById() {
        // First create a favourite
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FavouriteDto> postEntity = new HttpEntity<>(favouriteDto, postHeaders);
        restTemplate.exchange(baseUrl, HttpMethod.POST, postEntity, FavouriteDto.class);
        
        // Setup for DELETE request
        String dateParam = likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT));
        String url = baseUrl + "/1/101/" + dateParam;
        
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        
        // Execute
        ResponseEntity<Boolean> response = restTemplate.exchange(
                url,
                HttpMethod.DELETE,
                requestEntity,
                Boolean.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(true, response.getBody());
    }

    @Test
    @DisplayName("Should update favourite")
    public void testUpdateFavourite() {
        // First create a favourite
        HttpHeaders postHeaders = new HttpHeaders();
        postHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<FavouriteDto> postEntity = new HttpEntity<>(favouriteDto, postHeaders);
        restTemplate.exchange(baseUrl, HttpMethod.POST, postEntity, FavouriteDto.class);
        
        // Now update it
        HttpHeaders putHeaders = new HttpHeaders();
        putHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        // Create a modified DTO (in this case, we don't actually modify anything meaningful
        // since there aren't many fields to change)
        FavouriteDto updatedDto = FavouriteDto.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate)
                .build();
        
        HttpEntity<FavouriteDto> putEntity = new HttpEntity<>(updatedDto, putHeaders);
        
        // Execute
        ResponseEntity<FavouriteDto> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.PUT,
                putEntity,
                FavouriteDto.class
        );
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getUserId());
        assertEquals(101, response.getBody().getProductId());
    }
} 