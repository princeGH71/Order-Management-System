package com.prince.order_management_api.service;


import com.prince.order_management_api.dto.request.LoginRequest;
import com.prince.order_management_api.dto.request.RegisterRequest;
import com.prince.order_management_api.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register (RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
