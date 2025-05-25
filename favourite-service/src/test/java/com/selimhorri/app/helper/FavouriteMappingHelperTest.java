package com.selimhorri.app.helper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;

class FavouriteMappingHelperTest {

    private Favourite favourite;
    private FavouriteDto favouriteDto;
    private UserDto userDto;
    private ProductDto productDto;
    private LocalDateTime likeDate;

    @BeforeEach
    void setUp() {
        // Create test data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT);
        likeDate = LocalDateTime.parse("15-01-2023__10:30:00:000000", formatter);
        
        favourite = Favourite.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate)
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

        favouriteDto = FavouriteDto.builder()
                .userId(1)
                .productId(101)
                .likeDate(likeDate)
                .userDto(userDto)
                .productDto(productDto)
                .build();
    }

    @Test
    @DisplayName("Should map Favourite entity to FavouriteDto")
    void testMapEntityToDto() {
        // Act
        FavouriteDto result = FavouriteMappingHelper.map(favourite);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(favourite.getUserId());
        assertThat(result.getProductId()).isEqualTo(favourite.getProductId());
        assertThat(result.getLikeDate()).isEqualTo(favourite.getLikeDate());
        
        // Verify user and product mapping
        assertThat(result.getUserDto()).isNotNull();
        assertThat(result.getUserDto().getUserId()).isEqualTo(favourite.getUserId());
        assertThat(result.getProductDto()).isNotNull();
        assertThat(result.getProductDto().getProductId()).isEqualTo(favourite.getProductId());
    }

    @Test
    @DisplayName("Should map FavouriteDto to Favourite entity")
    void testMapDtoToEntity() {
        // Act
        Favourite result = FavouriteMappingHelper.map(favouriteDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(favouriteDto.getUserId());
        assertThat(result.getProductId()).isEqualTo(favouriteDto.getProductId());
        assertThat(result.getLikeDate()).isEqualTo(favouriteDto.getLikeDate());
    }
} 