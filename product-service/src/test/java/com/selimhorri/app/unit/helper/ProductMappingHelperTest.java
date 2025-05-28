package com.selimhorri.app.unit.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.helper.ProductMappingHelper;

class ProductMappingHelperTest {

    private Product product;
    private ProductDto productDto;
    private Category category;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        // Create test data
        category = Category.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();

        product = Product.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(599.99)
                .quantity(10)
                .category(category)
                .build();

        categoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();

        productDto = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(599.99)
                .quantity(10)
                .categoryDto(categoryDto)
                .build();
    }

    @Test
    @DisplayName("Should map Product entity to ProductDto")
    void testMapEntityToDto() {
        // Act
        ProductDto result = ProductMappingHelper.map(product);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(product.getProductId());
        assertThat(result.getProductTitle()).isEqualTo(product.getProductTitle());
        assertThat(result.getImageUrl()).isEqualTo(product.getImageUrl());
        assertThat(result.getSku()).isEqualTo(product.getSku());
        assertThat(result.getPriceUnit()).isEqualTo(product.getPriceUnit());
        assertThat(result.getQuantity()).isEqualTo(product.getQuantity());
        
        // Verify category mapping
        assertThat(result.getCategoryDto()).isNotNull();
        assertThat(result.getCategoryDto().getCategoryId()).isEqualTo(product.getCategory().getCategoryId());
        assertThat(result.getCategoryDto().getCategoryTitle()).isEqualTo(product.getCategory().getCategoryTitle());
        assertThat(result.getCategoryDto().getImageUrl()).isEqualTo(product.getCategory().getImageUrl());
    }

    @Test
    @DisplayName("Should map ProductDto to Product entity")
    void testMapDtoToEntity() {
        // Act
        Product result = ProductMappingHelper.map(productDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productDto.getProductId());
        assertThat(result.getProductTitle()).isEqualTo(productDto.getProductTitle());
        assertThat(result.getImageUrl()).isEqualTo(productDto.getImageUrl());
        assertThat(result.getSku()).isEqualTo(productDto.getSku());
        assertThat(result.getPriceUnit()).isEqualTo(productDto.getPriceUnit());
        assertThat(result.getQuantity()).isEqualTo(productDto.getQuantity());
        
        // Verify category mapping
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getCategoryId()).isEqualTo(productDto.getCategoryDto().getCategoryId());
        assertThat(result.getCategory().getCategoryTitle()).isEqualTo(productDto.getCategoryDto().getCategoryTitle());
        assertThat(result.getCategory().getImageUrl()).isEqualTo(productDto.getCategoryDto().getImageUrl());
    }
} 