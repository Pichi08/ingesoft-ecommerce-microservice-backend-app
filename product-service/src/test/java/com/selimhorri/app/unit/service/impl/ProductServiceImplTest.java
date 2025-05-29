package com.selimhorri.app.unit.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.impl.ProductServiceImpl;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product1;
    private Product product2;
    private ProductDto productDto1;
    private ProductDto productDto2;
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

        product1 = Product.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(599.99)
                .quantity(10)
                .category(category)
                .build();

        product2 = Product.builder()
                .productId(2)
                .productTitle("Laptop")
                .imageUrl("http://example.com/laptop.jpg")
                .sku("LAPTOP-456")
                .priceUnit(999.99)
                .quantity(5)
                .category(category)
                .build();

        categoryDto = CategoryDto.builder()
                .categoryId(1)
                .categoryTitle("Electronics")
                .imageUrl("http://example.com/electronics.jpg")
                .build();

        productDto1 = ProductDto.builder()
                .productId(1)
                .productTitle("Smartphone")
                .imageUrl("http://example.com/smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(599.99)
                .quantity(10)
                .categoryDto(categoryDto)
                .build();

        productDto2 = ProductDto.builder()
                .productId(2)
                .productTitle("Laptop")
                .imageUrl("http://example.com/laptop.jpg")
                .sku("LAPTOP-456")
                .priceUnit(999.99)
                .quantity(5)
                .categoryDto(categoryDto)
                .build();
    }

    @Test
    @DisplayName("Should find all products")
    void testFindAll() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductDto> result = productService.findAll();

        // Assert
        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getProductId()).isEqualTo(1);
        assertThat(result.get(0).getProductTitle()).isEqualTo("Smartphone");
        assertThat(result.get(1).getProductId()).isEqualTo(2);
        assertThat(result.get(1).getProductTitle()).isEqualTo("Laptop");
        verify(productRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find product by ID")
    void testFindById() {
        // Arrange
        when(productRepository.findById(1)).thenReturn(Optional.of(product1));

        // Act
        ProductDto result = productService.findById(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        assertThat(result.getProductTitle()).isEqualTo("Smartphone");
        assertThat(result.getSku()).isEqualTo("PHONE-123");
        assertThat(result.getCategoryDto().getCategoryId()).isEqualTo(1);
        assertThat(result.getCategoryDto().getCategoryTitle()).isEqualTo("Electronics");
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw exception when product ID not found")
    void testFindByIdNotFound() {
        // Arrange
        when(productRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.findById(999))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining("Product with id: 999 not found");
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should save product")
    void testSave() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(product1);
        
        // Act
        ProductDto result = productService.save(productDto1);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        assertThat(result.getProductTitle()).isEqualTo("Smartphone");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update existing product")
    void testUpdate() {
        // Arrange
        Product updatedProduct = Product.builder()
                .productId(1)
                .productTitle("Updated Smartphone")
                .imageUrl("http://example.com/updated-smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(699.99)
                .quantity(15)
                .category(category)
                .build();
        
        ProductDto updatedProductDto = ProductDto.builder()
                .productId(1)
                .productTitle("Updated Smartphone")
                .imageUrl("http://example.com/updated-smartphone.jpg")
                .sku("PHONE-123")
                .priceUnit(699.99)
                .quantity(15)
                .categoryDto(categoryDto)
                .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        
        // Act
        ProductDto result = productService.update(updatedProductDto);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(1);
        assertThat(result.getProductTitle()).isEqualTo("Updated Smartphone");
        assertThat(result.getPriceUnit()).isEqualTo(699.99);
        verify(productRepository, times(1)).save(any(Product.class));
    }
} 