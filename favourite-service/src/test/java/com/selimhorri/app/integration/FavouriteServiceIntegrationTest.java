package com.selimhorri.app.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceIntegrationTest {

    @Mock
    private FavouriteRepository favouriteRepository;
    
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite favourite1;
    private FavouriteId favouriteId1;
    private UserDto userDto;
    private ProductDto productDto;
    private LocalDateTime likeDate1;

    @BeforeEach
    void setUp() {
        // Create test data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT);
        likeDate1 = LocalDateTime.parse("15-01-2023__10:30:00:000000", formatter);
        
        favouriteId1 = new FavouriteId(1, 101, likeDate1);
        
        favourite1 = Favourite.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate1)
                .build();

        userDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        productDto = ProductDto.builder()
                .productId(101)
                .productTitle("Smartphone")
                .priceUnit(599.99)
                .build();
    }

    @Test
    @DisplayName("Should retrieve user details from user-service")
    void testUserServiceCommunication() {
        // Arrange
        when(favouriteRepository.findById(favouriteId1)).thenReturn(Optional.of(favourite1));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(productDto);

        // Act
        FavouriteDto result = favouriteService.findById(favouriteId1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserDto()).isNotNull();
        assertThat(result.getUserDto().getUserId()).isEqualTo(1);
        assertThat(result.getUserDto().getFirstName()).isEqualTo("John");
        assertThat(result.getUserDto().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("Should retrieve product details from product-service")
    void testProductServiceCommunication() {
        // Arrange
        when(favouriteRepository.findById(favouriteId1)).thenReturn(Optional.of(favourite1));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(productDto);

        // Act
        FavouriteDto result = favouriteService.findById(favouriteId1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductDto()).isNotNull();
        assertThat(result.getProductDto().getProductId()).isEqualTo(101);
        assertThat(result.getProductDto().getProductTitle()).isEqualTo("Smartphone");
        assertThat(result.getProductDto().getPriceUnit()).isEqualTo(599.99);
    }

    @Test
    @DisplayName("Should handle findAll and successfully retrieve user and product details")
    void testFindAllWithServiceCommunication() {
        // Arrange
        when(favouriteRepository.findAll()).thenReturn(Arrays.asList(favourite1));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(productDto);

        // Act
        List<FavouriteDto> results = favouriteService.findAll();

        // Assert
        assertThat(results).isNotNull().hasSize(1);
        FavouriteDto result = results.get(0);
        assertThat(result.getUserDto()).isNotNull();
        assertThat(result.getUserDto().getUserId()).isEqualTo(1);
        assertThat(result.getProductDto()).isNotNull();
        assertThat(result.getProductDto().getProductId()).isEqualTo(101);
    }

    @Test
    @DisplayName("Should handle service failure gracefully if user-service is unavailable")
    void testUserServiceUnavailable() {
        // Arrange
        when(favouriteRepository.findById(favouriteId1)).thenReturn(Optional.of(favourite1));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(null); // Simulating service unavailability
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(productDto);

        // Act & Assert
        FavouriteDto result = favouriteService.findById(favouriteId1);
        
        // Should still return favorite with product info even if user info is missing
        assertThat(result).isNotNull();
        assertThat(result.getUserDto()).isNull();
        assertThat(result.getProductDto()).isNotNull();
        assertThat(result.getProductDto().getProductId()).isEqualTo(101);
    }

    @Test
    @DisplayName("Should handle service failure gracefully if product-service is unavailable")
    void testProductServiceUnavailable() {
        // Arrange
        when(favouriteRepository.findById(favouriteId1)).thenReturn(Optional.of(favourite1));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(null); // Simulating service unavailability

        // Act & Assert
        FavouriteDto result = favouriteService.findById(favouriteId1);
        
        // Should still return favorite with user info even if product info is missing
        assertThat(result).isNotNull();
        assertThat(result.getUserDto()).isNotNull();
        assertThat(result.getUserDto().getUserId()).isEqualTo(1);
        assertThat(result.getProductDto()).isNull();
    }
} 