package com.prince.order_management_api.service;

import com.prince.order_management_api.dto.request.ProductRequest;
import com.prince.order_management_api.dto.response.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProductById(UUID id);
    List<ProductResponse> getAllProducts();
    ProductResponse updateProduct(UUID id, ProductRequest request);
    void deleteProduct(UUID id);
}
