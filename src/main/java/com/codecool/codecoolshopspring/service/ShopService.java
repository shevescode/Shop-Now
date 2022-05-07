package com.codecool.codecoolshopspring.service;

import com.codecool.codecoolshopspring.model.Order;
import com.codecool.codecoolshopspring.model.Supplier;
import com.codecool.codecoolshopspring.repository.OrderRepository;
import com.codecool.codecoolshopspring.repository.ProductCategoryRepository;
import com.codecool.codecoolshopspring.repository.ProductRepository;
import com.codecool.codecoolshopspring.model.Product;
import com.codecool.codecoolshopspring.model.ProductCategory;
import com.codecool.codecoolshopspring.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ShopService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final OrderRepository orderRepository;
    private final SupplierRepository supplierRepository;

    public ProductCategory getProductCategory(int categoryId){
        return productCategoryRepository.find(categoryId).orElseThrow();
    }

    public Supplier getSupplier(int supplierId){
        return supplierRepository.findById(supplierId).orElseThrow();
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<ProductCategory> getAllProductCategories() {
        return productCategoryRepository.findAll();
    }

    public Optional<Order> getUserOrder(String userName) {
        return orderRepository.findByUserName(userName);
    }

    public Map<String, String> clearUserOrder(String userName){
        Optional<Order> order = getUserOrder(userName);
        Map<String, String> response = new HashMap<>();
        if (order.isPresent()){
            order.get().setOrderedProducts(new HashMap<>());
            response.put("status", "order clear!");
        } else {
            response.put("status", "order not found!");
        }
        return response;
    }

    public Optional<Product> getProductById(int id){
        return productRepository.findById(id);
    }

    private void putNewOrder(Order order){
        orderRepository.save(order);
    }

    public Map<String, Integer> addProductToOrder(String userName, Integer productId) {
        Optional<Product> optProduct = getProductById(productId);
        Map<String, Integer> response = new HashMap<>();
        optProduct.ifPresent(product -> setOrder(product, userName, response, true));
        return response;
    }

    public Map<String, Integer> removeProductFromOrder(String userName, Integer productId) {
        Optional<Product> optProduct = getProductById(productId);
        Map<String, Integer> response = new HashMap<>();
        optProduct.ifPresent(product -> setOrder(product, userName, response, false));
        return response;
    }

    private void setOrder(Product product, String userName, Map<String, Integer> response, boolean add){
        Optional<Order> optOrder = getUserOrder(userName);
        Order order;
        if (optOrder.isEmpty()){
            order = new Order(orderRepository.findLastOrderId()+1, userName);
            putNewOrder(order);
        } else {
            order = optOrder.get();
        }
        if (add) {
            order.addToOrder(product);
        } else {
            order.removeFromOrder(product);
        }
        response.put("productsCount", order.countProducts());
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    public BigDecimal getTotalOrderValue(Optional<Order> order) {
        BigDecimal result = new BigDecimal( 0);
        Order orderGet = order.get();
        for (Product product: orderGet.getOrderedProducts().keySet()) {
            result = result.add(product.getDefaultPrice().multiply(new BigDecimal(orderGet.get(product))));
        }
        return result;
    }
}
