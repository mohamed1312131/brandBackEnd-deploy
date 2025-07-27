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

    private void sendOrderConfirmationEmail(Checkout checkout) {
        if (checkout.getEmail() == null || checkout.getEmail().isEmpty()) return;

        StringBuilder productList = new StringBuilder();
        for (Checkout.OrderedProduct item : checkout.getProducts()) {
            productList.append("<li>")
                    .append(item.getProductName())
                    .append(" - Size: ")
                    .append(item.getSize())
                    .append(" - Qty: ")
                    .append(item.getQuantity())
                    .append("</li>");
        }

        String html = String.format("""
<!DOCTYPE html>
<html lang=\"en\">
<head>
  <meta charset=\"UTF-8\">
  <style>
    body { margin: 0; padding: 0; background-color: #f5f5f5; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; }
    .container { max-width: 600px; margin: auto; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05); }
    .header { padding: 40px 30px 10px; text-align: center; }
    .header img { max-width: 120px; border-radius: 6px; }
    .subtitle { text-transform: uppercase; font-size: 12px; color: #999; margin-top: 20px; }
    .title { font-size: 24px; font-weight: bold; color: #111; margin: 10px 0; }
    .content { padding: 0 30px 30px; color: #333; font-size: 15px; line-height: 1.6; }
    .cta-button { display: inline-block; background-color: #111; color: #fff !important; padding: 14px 26px; text-decoration: none; border-radius: 6px; margin-top: 30px; }
    .footer { text-align: center; font-size: 12px; color: #aaa; padding: 20px; }
    ul { padding-left: 1rem; }
  </style>
</head>
<body>
  <div class=\"container\">
    <div class=\"header\">
      <img src=\"https://yourdomain.com/logo.png\" alt=\"Brand Logo\" />
      <div class=\"subtitle\">Merci pour votre commande</div>
      <div class=\"title\">Commande %s</div>
    </div>
    <div class=\"content\">
      <p>Bonjour,</p>
      <p>Nous avons bien reçu votre commande <strong>%s</strong>. Voici un résumé de votre achat :</p>
      <ul>%s</ul>
      <p><strong>Total:</strong> %.2f TND</p>
      <p>Nous vous contacterons bientôt au <strong>%s</strong> pour confirmer la livraison.</p>
      <a class=\"cta-button\" href=\"https://yourdomain.com/orders/%s\">Voir ma commande</a>
    </div>
    <div class=\"footer\">
      &copy; %d Votre Boutique. Tous droits réservés.
    </div>
  </div>
</body>
</html>
""",
                checkout.getOrderId(),
                checkout.getOrderId(),
                productList,
                checkout.getGrandTotal(),
                checkout.getPhone(),
                checkout.getId(),
                Calendar.getInstance().get(Calendar.YEAR)
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(checkout.getEmail());
            helper.setSubject("Confirmation de commande - " + checkout.getOrderId());
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
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
