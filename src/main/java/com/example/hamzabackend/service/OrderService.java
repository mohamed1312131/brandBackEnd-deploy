package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.*;
import com.example.hamzabackend.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final VisitorRepository visitorRepository;
    private final CartRepository cartRepository;
    private final CheckoutRepository checkoutRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private static final List<String> VALID_STATUSES = List.of("PENDING", "PROCESSING", "COMPLETED", "CANCELLED");
    private final JavaMailSender mailSender;
    /**
     * Save the checkout (order) submitted by user.
     */

    private String generateOrderId() {
        return "HZ" + (int)(Math.random() * 90000 + 10000); // generates hz10000–hz99999
    }

    @Transactional
    public Checkout placeOrder(Checkout checkout) {
        double total = 0.0;

        for (Checkout.OrderedProduct item : checkout.getProducts()) {
            Optional<Product> optionalProduct = productRepository.findById(item.getProductId());
            if (optionalProduct.isEmpty()) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }

            Product product = optionalProduct.get();
            item.setProductName(product.getTitle());
            total += product.getPrice() * item.getQuantity();
        }

        checkout.setOrderId(generateOrderId());
        checkout.setTotal(total);
        checkout.setGrandTotal(total + 5.0);
        checkout.setStatus("PENDING");
        checkout.setCreatedAt(Instant.now());
        checkout.setDelivered(false);

        Checkout saved = checkoutRepository.save(checkout);
        sendOrderConfirmationEmail(saved);

        return saved;
    }

// In OrderService.java

    private void sendOrderConfirmationEmail(Checkout checkout) {
        if (checkout.getEmail() == null || checkout.getEmail().isEmpty()) {
            // Log this or handle it, but don't proceed without an email address
            System.err.println("Skipping order confirmation email: No email address for order " + checkout.getOrderId());
            return;
        }

        // --- Configuration ---
        // IMPORTANT: Replace this with your application's public domain/IP address.
        // This must be accessible from the internet for the logo to appear in emails.
        // For local testing: "http://localhost:8080" (if your app runs on port 8080)
        // For production: "https://www.yourbrand.com"
        String baseUrl = "https://www.edityam.com";
        String logoUrl = baseUrl + "/images/logo.png";


        // --- Build Product List ---
        StringBuilder productListHtml = new StringBuilder();
        for (Checkout.OrderedProduct item : checkout.getProducts()) {
            productListHtml.append(String.format("""
            <tr>
              <td style="padding: 15px 0; border-bottom: 1px solid #eeeeee;">
                <span style="font-weight: bold;">%s</span><br>
                <span style="color: #666666; font-size: 14px;">Size: %s</span>
              </td>
              <td style="padding: 15px 0; border-bottom: 1px solid #eeeeee; text-align: right;">%d</td>
              <td style="padding: 15px 0; border-bottom: 1px solid #eeeeee; text-align: right;">%.2f TND</td>
            </tr>
        """, item.getProductName(), item.getSize(), item.getQuantity()));
        }

        // --- Main Email Template ---
        String htmlTemplate = String.format("""
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmation</title>
    <style>
        body { margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4; }
        .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border: 1px solid #dddddd; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .header { background-color: #111111; color: #ffffff; padding: 20px; text-align: center; border-top-left-radius: 8px; border-top-right-radius: 8px; }
        .header img { max-width: 150px; }
        .content { padding: 30px; color: #333333; line-height: 1.6; }
        .content h1 { color: #111111; font-size: 24px; }
        .order-details { width: 100%%; border-collapse: collapse; margin: 20px 0; }
        .order-details th { background-color: #f9f9f9; padding: 10px; text-align: left; border-bottom: 1px solid #dddddd; }
        .summary { margin-top: 30px; padding-top: 20px; border-top: 2px solid #eeeeee; }
        .summary-table { width: 100%%; }
        .summary-table td { padding: 5px 0; }
        .total { font-weight: bold; font-size: 18px; color: #111111; }
        .cta-button { display: inline-block; background-color: #111111; color: #ffffff !important; padding: 15px 30px; text-decoration: none; border-radius: 5px; font-size: 16px; margin-top: 20px; }
        .footer { text-align: center; font-size: 12px; color: #888888; padding: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <img src="%s" alt="Brand Logo">
        </div>
        <div class="content">
            <h1>Your Order is Confirmed!</h1>
            <p>Hi %s,</p>
            <p>Thank you for your purchase. We've received your order and are getting it ready for shipment. You will be contacted soon at <strong>%s</strong> to confirm delivery details.</p>
            
            <h3 style="color: #111111;">Order #%s</h3>
            
            <table class="order-details">
                <thead>
                    <tr>
                        <th style="width: 60%%;">Item</th>
                        <th style="text-align: right;">Qty</th>
                        <th style="text-align: right;">Price</th>
                    </tr>
                </thead>
                <tbody>
                    %s
                </tbody>
            </table>
            
            <table class="summary-table">
                <tr>
                    <td>Subtotal</td>
                    <td style="text-align: right;">%.2f TND</td>
                </tr>
                <tr>
                    <td>Shipping</td>
                    <td style="text-align: right;">%.2f TND</td>
                </tr>
                <tr>
                    <td class="total">Grand Total</td>
                    <td class="total" style="text-align: right;">%.2f TND</td>
                </tr>
            </table>
            
            <p style="text-align: center;">
                <a href="%s" class="cta-button">View Your Order</a>
            </p>
        </div>
        <div class="footer">
            <p>&copy; %d YourBrand. All rights reserved.</p>
            <p>123 Fashion Street, Tunis, Tunisia</p>
        </div>
    </div>
</body>
</html>
""",
                logoUrl,
                checkout.getFirstName(), // Assuming you have a 'getFirstName()' on your Checkout object
                checkout.getPhone(),
                checkout.getOrderId(),
                productListHtml.toString(),
                checkout.getTotal(),
                (checkout.getGrandTotal() - checkout.getTotal()), // Shipping cost calculation
                checkout.getGrandTotal(),
                Calendar.getInstance().get(Calendar.YEAR)
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(checkout.getEmail());
            helper.setFrom("no-reply@yourbrand.com"); // It's good practice to set a 'from' address
            helper.setSubject("Your Order Confirmation: " + checkout.getOrderId());
            helper.setText(htmlTemplate, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            // It's better to use a logger here in a real application
            e.printStackTrace();
        }
    }
    public List<Checkout> getAllOrders() {
        return checkoutRepository.findAll();
    }




    /**
     * Clear the cart once order is placed (optional).
     */
    @Transactional
    public void clearCart(String cartId) {
        cartRepository.deleteById(cartId);
    }
    @Transactional
    public Cart addItemToCart(String cartId, Cart.CartItem newItem) {
        Cart cart = cartRepository.findById(cartId).orElseGet(() -> {
            Cart c = new Cart();
            c.setId(cartId);
            c.setCreatedAt(Instant.now());
            c.setProducts(new java.util.ArrayList<>());
            return c;
        });

        // Check if item already exists (same product + size)
        boolean updated = false;
        for (Cart.CartItem item : cart.getProducts()) {
            if (item.getProductId().equals(newItem.getProductId())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                updated = true;
                break;
            }
        }

        if (!updated) {
            cart.getProducts().add(newItem);
        }

        // Recalculate totals
        double total = 0.0;
        for (Cart.CartItem item : cart.getProducts()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            total += product.getPrice() * item.getQuantity();
        }

        cart.setTotal(total);
        cart.setGrandTotal(total + 5); // optional delivery fee

        return cartRepository.save(cart);
    }
    @Transactional
    public Cart removeItemFromCart(String cartId, String productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getProducts().removeIf(item -> item.getProductId().equals(productId));

        // Recalculate total
        double total = 0.0;
        for (Cart.CartItem item : cart.getProducts()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));
            total += product.getPrice() * item.getQuantity();
        }

        cart.setTotal(total);
        cart.setGrandTotal(total + 5);

        return cartRepository.save(cart);
    }
    public Map<String, Double> getMonthlySalesByYear(int year) {
        List<Checkout> orders = checkoutRepository.findAll();

        Map<String, Double> salesByMonth = new TreeMap<>(); // TreeMap keeps months sorted
        for (int m = 1; m <= 12; m++) {
            salesByMonth.put(String.format("%02d", m), 0.0);
        }

        for (Checkout order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) continue; // ✅ only completed
            if (order.getCreatedAt() != null &&
                    order.getCreatedAt().atZone(ZoneId.systemDefault()).getYear() == year) {
                String month = String.format("%02d", order.getCreatedAt().atZone(ZoneId.systemDefault()).getMonthValue());
                salesByMonth.put(month, salesByMonth.get(month) + order.getTotal());
            }
        }

        return salesByMonth;
    }
    public Map<String, Double> getSalesTotalForYears(int year) {
        List<Checkout> orders = checkoutRepository.findAll();

        double thisYearTotal = 0;
        double lastYearTotal = 0;

        for (Checkout order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) continue; // ✅ only completed

            if (order.getCreatedAt() != null) {
                int orderYear = order.getCreatedAt().atZone(ZoneId.systemDefault()).getYear();
                if (orderYear == year) thisYearTotal += order.getTotal();
                if (orderYear == (year - 1)) lastYearTotal += order.getTotal();
            }
        }

        Map<String, Double> totals = new HashMap<>();
        totals.put("current", thisYearTotal);
        totals.put("previous", lastYearTotal);
        return totals;
    }

    public Visitor logVisit(String ip) {
        Visitor visitor = new Visitor();
        visitor.setIpAddress(ip);
        visitor.setTimestamp(Instant.now());
        return visitorRepository.save(visitor);
    }

    public long countByMonthAndYear(int month, int year) {
        return visitorRepository.findAll().stream()
                .filter(v -> v.getTimestamp() != null &&
                        v.getTimestamp().atZone(java.time.ZoneId.systemDefault()).getMonthValue() == month &&
                        v.getTimestamp().atZone(java.time.ZoneId.systemDefault()).getYear() == year)
                .count();
    }

    public long countByMonthYearComparison(int month, int year) {
        // This year
        long current = countByMonthAndYear(month, year);
        // Same month, last year
        long previous = countByMonthAndYear(month, year - 1);
        return current - previous;
    }

    public Map<String, Double> getSalesByRegion(int year, int month) {
        List<Checkout> orders = checkoutRepository.findAll();
        Map<String, Double> regionSales = new HashMap<>();

        for (Checkout order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) continue; // ✅ only completed
            if (order.getCreatedAt() == null) continue;

            ZonedDateTime date = order.getCreatedAt().atZone(ZoneId.systemDefault());
            if (date.getYear() == year && date.getMonthValue() == month) {
                String region = order.getRegion() != null ? order.getRegion() : "Unknown";
                regionSales.put(region, regionSales.getOrDefault(region, 0.0) + order.getTotal());
            }
        }

        return regionSales;
    }
    public Map<String, Double> getSalesByCategory(int year, int month) {
        List<Checkout> orders = checkoutRepository.findAll();

        Map<String, Double> salesByCategory = new HashMap<>();

        for (Checkout order : orders) {
            if (!"COMPLETED".equalsIgnoreCase(order.getStatus())) continue; // ✅ only completed
            if (order.getCreatedAt() == null) continue;
            ZonedDateTime zdt = order.getCreatedAt().atZone(ZoneId.systemDefault());

            if (zdt.getYear() != year || zdt.getMonthValue() != month) continue;

            for (Checkout.OrderedProduct item : order.getProducts()) {
                Optional<Product> optProduct = productRepository.findById(item.getProductId());
                if (optProduct.isEmpty()) continue;

                Product product = optProduct.get();
                if (product.getCategory() == null) continue;

                String categoryName = product.getCategory().getName(); // Ensure getName() is available
                double total = item.getQuantity() * product.getPrice();

                salesByCategory.put(categoryName,
                        salesByCategory.getOrDefault(categoryName, 0.0) + total);
            }
        }

        return salesByCategory;
    }

    @Transactional
    public Checkout updateOrderStatus(String checkoutId, String newStatus) {
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        String oldStatus = checkout.getStatus();
        newStatus = newStatus.toUpperCase();

        // ✅ Reset stock if moving from COMPLETED to a non-completed status
        if ("COMPLETED".equalsIgnoreCase(oldStatus) && !"COMPLETED".equalsIgnoreCase(newStatus)) {
            for (Checkout.OrderedProduct item : checkout.getProducts()) {
                List<ProductVariant> variants = productVariantRepository.findByProductId(item.getProductId());

                for (ProductVariant variant : variants) {
                    for (SizeVariant sizeVariant : variant.getSizes()) {
                        if (sizeVariant.getSize().equalsIgnoreCase(item.getSize())) {
                            // Reverse the stock and sold count
                            sizeVariant.setStock(sizeVariant.getStock() + item.getQuantity());
                            sizeVariant.setSold(Math.max(0, sizeVariant.getSold() - item.getQuantity()));
                            productVariantRepository.save(variant);
                            break;
                        }
                    }
                }
            }
        }

        // ✅ Deduct stock only if transitioning TO COMPLETED
        else if (!"COMPLETED".equalsIgnoreCase(oldStatus) && "COMPLETED".equalsIgnoreCase(newStatus)) {
            for (Checkout.OrderedProduct item : checkout.getProducts()) {
                List<ProductVariant> variants = productVariantRepository.findByProductId(item.getProductId());

                boolean updated = false;
                for (ProductVariant variant : variants) {
                    for (SizeVariant sizeVariant : variant.getSizes()) {
                        if (sizeVariant.getSize().equalsIgnoreCase(item.getSize())) {
                            if (sizeVariant.getStock() < item.getQuantity()) {
                                throw new RuntimeException("Insufficient stock for product: " + item.getProductName());
                            }

                            sizeVariant.setStock(sizeVariant.getStock() - item.getQuantity());
                            sizeVariant.setSold(sizeVariant.getSold() + item.getQuantity());
                            productVariantRepository.save(variant);
                            updated = true;
                            break;
                        }
                    }
                    if (updated) break;
                }

                if (!updated) {
                    throw new RuntimeException("Matching size variant not found for product: " + item.getProductName());
                }
            }
        }

        // ✅ Set the new status
        checkout.setStatus(newStatus);

        return checkoutRepository.save(checkout);
    }










}
