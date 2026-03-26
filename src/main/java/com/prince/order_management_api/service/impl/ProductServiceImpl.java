package com.prince.order_management_api.service.impl;

import com.prince.order_management_api.dto.request.ProductRequest;
import com.prince.order_management_api.dto.response.ProductResponse;
import com.prince.order_management_api.entity.Product;
import com.prince.order_management_api.exception.ApiException;
import com.prince.order_management_api.repository.ProductRepository;
import com.prince.order_management_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ApiException("Product not found.", HttpStatus.NOT_FOUND)
        );
        return mapToResponse(product);
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> productList = productRepository.findAll();
        return productList.stream()
                .map(product -> mapToResponse(product))
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ApiException("Product not found.", HttpStatus.NOT_FOUND)
        );

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ApiException("Product not found.", HttpStatus.NOT_FOUND)
        );
        productRepository.delete(product);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
