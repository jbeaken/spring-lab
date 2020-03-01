package com.jack.spring.gateway.cloudgateway.domain;

import com.jack.spring.gateway.domain.Product;

import java.util.List;

/**
 * @author Benjamin Wilms
 */
public class ProductResponse {

    private ResponseType responseType;
    private List<Product> products;

    public ProductResponse(ResponseType responseType, List<Product> products) {
        this.responseType = responseType;
        this.products = products;
    }

    public ProductResponse() {}

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public List<Product> getProducts() {
        return products;
    }
}
