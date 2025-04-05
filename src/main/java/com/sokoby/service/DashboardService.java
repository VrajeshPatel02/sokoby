package com.sokoby.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sokoby.entity.Customer;
import com.sokoby.entity.Order;
import com.sokoby.entity.Product;
import com.sokoby.enums.OrderStatus;
import com.sokoby.repository.CustomerRepository;
import com.sokoby.repository.OrderRepository;
import com.sokoby.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public Map<String, Object> getDashboardStats(String storeId) {
        log.info("Getting dashboard stats for store: {}", storeId);
        
        try {
            UUID storeUuid = UUID.fromString(storeId);
            log.info("Converted storeId to UUID: {}", storeUuid);
            
            List<Order> orders = orderRepository.findByStoreId(storeUuid);
            log.info("Found {} orders for store", orders.size());
            
            List<Product> products = productRepository.findByStoreId(storeUuid);
            log.info("Found {} products for store", products.size());
            
            List<Customer> customers = customerRepository.findByStoreId(storeUuid);
            log.info("Found {} customers for store", customers.size());

            // Calculate total sales
            double totalSales = orders.stream()
                    .filter(order -> order.getStatus().equals(OrderStatus.CONFIRMED) || order.getStatus().equals(OrderStatus.DELIVERED))
                    .mapToDouble(order -> order.getTotalAmount() != null ? order.getTotalAmount() : 0.0)
                    .sum();
            log.info("Total sales: {}", totalSales);

            // Calculate total orders - include all orders regardless of status
            int totalOrders = orders.size();
            log.info("Total orders: {}", totalOrders);

            // Calculate total customers
            int totalCustomers = customers.size();
            log.info("Total customers: {}", totalCustomers);

            // Calculate average order value - only for confirmed/delivered orders
            long confirmedOrdersCount = orders.stream()
                    .filter(order -> order.getStatus().equals(OrderStatus.CONFIRMED) || order.getStatus().equals(OrderStatus.DELIVERED))
                    .count();
            double averageOrderValue = confirmedOrdersCount > 0 
                    ? totalSales / confirmedOrdersCount
                    : 0.0;
            log.info("Average order value: {}", averageOrderValue);

            // Group orders by month
            List<Map<String, Object>> salesByMonth = groupOrdersByMonth(orders);
            log.info("Sales by month data points: {}", salesByMonth.size());

            // Group products by category
            List<Map<String, Object>> salesByCategory = groupProductsByCategory(products);
            log.info("Sales by category data points: {}", salesByCategory.size());

            // Mock traffic sources data
            List<Map<String, Object>> trafficSources = Arrays.asList(
                    createTrafficSource("Direct", 40),
                    createTrafficSource("Organic Search", 30),
                    createTrafficSource("Social Media", 15),
                    createTrafficSource("Referral", 10),
                    createTrafficSource("Email", 5)
            );

            // Get recent orders
            List<Map<String, Object>> recentOrders = orders.stream()
                    .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                    .limit(5)
                    .map(this::convertOrderToMap)
                    .collect(Collectors.toList());
            log.info("Recent orders: {}", recentOrders.size());

            // Get top products
            List<Map<String, Object>> topProducts = products.stream()
                    .sorted(Comparator.comparing(Product::getPrice).reversed())
                    .limit(4)
                    .map(this::convertProductToMap)
                    .collect(Collectors.toList());
            log.info("Top products: {}", topProducts.size());

            // Get recent customers
            List<Map<String, Object>> recentCustomers = customers.stream()
                    .sorted(Comparator.comparing(Customer::getCreatedAt).reversed())
                    .limit(3)
                    .map(this::convertCustomerToMap)
                    .collect(Collectors.toList());
            log.info("Recent customers: {}", recentCustomers.size());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSales", totalSales);
            stats.put("totalOrders", totalOrders);
            stats.put("totalCustomers", totalCustomers);
            stats.put("averageOrderValue", averageOrderValue);
            stats.put("salesByMonth", salesByMonth);
            stats.put("salesByCategory", salesByCategory);
            stats.put("trafficSources", trafficSources);
            stats.put("recentOrders", recentOrders);
            stats.put("topProducts", topProducts);
            stats.put("recentCustomers", recentCustomers);

            log.info("Dashboard stats created successfully");
            return stats;
        } catch (Exception e) {
            log.error("Error getting dashboard stats for store: {}", storeId, e);
            throw e;
        }
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("id", order.getId());
        orderMap.put("totalAmount", order.getTotalAmount());
        orderMap.put("status", order.getStatus());
        orderMap.put("createdAt", order.getCreatedAt());
        return orderMap;
    }

    private Map<String, Object> convertProductToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("title", product.getTitle());
        productMap.put("price", product.getPrice());
        productMap.put("status", product.getStatus());
        return productMap;
    }

    private Map<String, Object> convertCustomerToMap(Customer customer) {
        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("id", customer.getId());
        customerMap.put("name", customer.getName());
        customerMap.put("email", customer.getEmail());
        customerMap.put("createdAt", customer.getCreatedAt());
        return customerMap;
    }

    private List<Map<String, Object>> groupOrdersByMonth(List<Order> orders) {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        List<Map<String, Object>> salesData = new ArrayList<>();

        for (String month : months) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("name", month);
            monthData.put("value", 0);
            monthData.put("orders", 0);
            salesData.add(monthData);
        }

        for (Order order : orders) {
            if (order.getCreatedAt() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(order.getCreatedAt());
                int monthIndex = cal.get(Calendar.MONTH);
                Map<String, Object> monthData = salesData.get(monthIndex);
                monthData.put("value", ((Number) monthData.get("value")).doubleValue() + (order.getTotalAmount() != null ? order.getTotalAmount() : 0.0));
                monthData.put("orders", ((Number) monthData.get("orders")).intValue() + 1);
            }
        }

        return salesData;
    }

    private List<Map<String, Object>> groupProductsByCategory(List<Product> products) {
        Map<String, Double> categories = new HashMap<>();

        for (Product product : products) {
            String category = product.getCollections() != null && !product.getCollections().isEmpty()
                    ? String.valueOf(product.getCollections().get(0).getType())
                    : "Other";
            categories.merge(category, product.getPrice(), Double::sum);
        }

        return categories.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> categoryData = new HashMap<>();
                    categoryData.put("name", entry.getKey());
                    categoryData.put("value", entry.getValue());
                    return categoryData;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> createTrafficSource(String name, int value) {
        Map<String, Object> source = new HashMap<>();
        source.put("name", name);
        source.put("value", value);
        return source;
    }
} 