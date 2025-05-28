package com.selimhorri.app.unit.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;
    
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite favourite1;
    private Favourite favourite2;
    private FavouriteDto favouriteDto1;
    private FavouriteDto favouriteDto2;
    private FavouriteId favouriteId1;
    private FavouriteId favouriteId2;
    private UserDto userDto;
    private ProductDto productDto;
    private LocalDateTime likeDate1;
    private LocalDateTime likeDate2;

    @BeforeEach
    void setUp() {
        // Create test data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT);
        likeDate1 = LocalDateTime.parse("15-01-2023__10:30:00:000000", formatter);
        likeDate2 = LocalDateTime.parse("20-02-2023__14:45:00:000000", formatter);
        
        favouriteId1 = new FavouriteId(1, 101, likeDate1);
        favouriteId2 = new FavouriteId(2, 102, likeDate2);
        
        favourite1 = Favourite.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate1)
                .build();

        favourite2 = Favourite.builder()
                .userId(2)
                .productId(102)
                .likeDate(likeDate2)
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

        favouriteDto1 = FavouriteDto.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate1)
                .userDto(userDto)
                .productDto(productDto)
                .build();

        favouriteDto2 = FavouriteDto.builder()
                .userId(2)
                .productId(102)
                .likeDate(likeDate2)
                .build();
    }

    @Test
    @DisplayName("Should find all favourites")
    void testFindAll() {
        // Arrange
        when(favouriteRepository.findAll()).thenReturn(Arrays.asList(favourite1, favourite2));
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/1"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.USER_SERVICE_API_URL + "/2"), eq(UserDto.class)))
            .thenReturn(userDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/101"), eq(ProductDto.class)))
            .thenReturn(productDto);
        when(restTemplate.getForObject(eq(AppConstant.DiscoveredDomainsApi.PRODUCT_SERVICE_API_URL + "/102"), eq(ProductDto.class)))
            .thenReturn(productDto);

        // Act
        List<FavouriteDto> result = favouriteService.findAll();

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
        assertThat(result.get(0).getProductId()).isEqualTo(101);
        assertThat(result.get(1).getUserId()).isEqualTo(2);
        assertThat(result.get(1).getProductId()).isEqualTo(102);
        verify(favouriteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find favourite by ID")
    void testFindById() {
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
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getProductId()).isEqualTo(101);
        assertThat(result.getLikeDate()).isEqualTo(likeDate1);
        verify(favouriteRepository, times(1)).findById(favouriteId1);
    }

    @Test
    @DisplayName("Should throw exception when favourite ID not found")
    void testFindByIdNotFound() {
        // Arrange
        when(favouriteRepository.findById(any(FavouriteId.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> favouriteService.findById(favouriteId1))
            .isInstanceOf(FavouriteNotFoundException.class)
            .hasMessageContaining("Favourite with id");
        verify(favouriteRepository, times(1)).findById(favouriteId1);
    }

    @Test
    @DisplayName("Should save favourite")
    void testSave() {
        // Arrange
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite1);
        
        // Act
        FavouriteDto result = favouriteService.save(favouriteDto1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getProductId()).isEqualTo(101);
        assertThat(result.getLikeDate()).isEqualTo(likeDate1);
        verify(favouriteRepository, times(1)).save(any(Favourite.class));
    }

    @Test
    @DisplayName("Should delete favourite by ID")
    void testDeleteById() {
        // Arrange
        doNothing().when(favouriteRepository).deleteById(favouriteId1);
        
        // Act
        favouriteService.deleteById(favouriteId1);
        
        // Assert
        verify(favouriteRepository, times(1)).deleteById(favouriteId1);
    }
} 