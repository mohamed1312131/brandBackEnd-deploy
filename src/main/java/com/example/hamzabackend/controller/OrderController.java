package com.example.hamzabackend.controller;

import com.example.hamzabackend.DTO.TopSellingProductDTO;
import com.example.hamzabackend.entity.Cart;
import com.example.hamzabackend.entity.Checkout;
import com.example.hamzabackend.entity.Visitor;
import com.example.hamzabackend.repository.CartRepository;
import com.example.hamzabackend.repository.OrderRepositoryCustom;
import com.example.hamzabackend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartRepository cartRepository;
    private final OrderRepositoryCustom orderRepositoryCustom;


    @PostMapping("/place")
    public ResponseEntity<Checkout> placeOrder(@RequestBody Checkout checkout) {
        return ResponseEntity.ok(orderService.placeOrder(checkout));
    }

    @GetMapping
    public List<Checkout> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Checkout> updateStatus(@PathVariable String id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @DeleteMapping("/cart/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable String cartId) {
        orderService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/cart/{cartId}/add")
    public ResponseEntity<Cart> addToCart(
            @PathVariable String cartId,
            @RequestBody Cart.CartItem item
    ) {
        return ResponseEntity.ok(orderService.addItemToCart(cartId, item));
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<Cart> getCart(@PathVariable String cartId) {
        return ResponseEntity.of(cartRepository.findById(cartId));
    }
    @DeleteMapping("/cart/{cartId}/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable String cartId, @PathVariable String productId) {
        return ResponseEntity.ok(orderService.removeItemFromCart(cartId, productId));
    }
    @GetMapping("/sales/yearly")
    public ResponseEntity<Map<String, Double>> getSalesByYear(@RequestParam int year) {
        return ResponseEntity.ok(orderService.getMonthlySalesByYear(year));
    }
    @GetMapping("/sales/year-comparison")
    public ResponseEntity<Map<String, Double>> getSalesComparison(@RequestParam int year) {
        return ResponseEntity.ok(orderService.getSalesTotalForYears(year));
    }
    @GetMapping("/visitors/monthly")
    public ResponseEntity<Map<String, Integer>> getMonthlyVisitors(
            @RequestParam int month,
            @RequestParam int year
    ) {
        // Dummy data for now
        Map<String, Integer> result = new HashMap<>();
        result.put("current", 420);  // Replace with actual lookup
        result.put("previous", 390);
        return ResponseEntity.ok(result);
    }
    @PostMapping("track")
    public Visitor trackVisit(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return orderService.logVisit(ip);
    }

    @GetMapping("/monthly")
    public Map<String, Long> getMonthlyVisitorStats(@RequestParam int month, @RequestParam int year) {
        long current = orderService.countByMonthAndYear(month, year);
        long previous = orderService.countByMonthAndYear(month, year - 1);

        Map<String, Long> result = new HashMap<>();
        result.put("current", current);
        result.put("previous", previous);
        return result;
    }
    @GetMapping("/sales/by-region")
    public ResponseEntity<Map<String, Double>> getSalesByRegion(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(orderService.getSalesByRegion(year, month));
    }
    @GetMapping("/sales/by-category")
    public ResponseEntity<Map<String, Double>> getSalesByCategory(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(orderService.getSalesByCategory(year, month));
    }
    @GetMapping("/top-products")
    public List<TopSellingProductDTO> getTopProducts(
            @RequestParam int year,
            @RequestParam int month
    ) {
        return orderRepositoryCustom.findTopSellingProducts(year, month, 5);
    }




}
