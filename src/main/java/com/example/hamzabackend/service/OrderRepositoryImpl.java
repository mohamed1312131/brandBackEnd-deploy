package com.example.hamzabackend.service;



import com.example.hamzabackend.DTO.TopSellingProductDTO;
import com.example.hamzabackend.entity.Checkout;
import com.example.hamzabackend.entity.Product;
import com.example.hamzabackend.entity.ProductVariant;
import com.example.hamzabackend.entity.SizeVariant;
import com.example.hamzabackend.repository.OrderRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<TopSellingProductDTO> findTopSellingProducts(int year, int month, int limit) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1);

        // Stage 1: Match orders within selected month/year
        MatchOperation match = Aggregation.match(
                Criteria.where("createdAt")
                        .gte(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        .lt(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                        .and("status").is("COMPLETED") // ✅ Only completed orders
        );

        // Stage 2: Unwind products
        UnwindOperation unwindProducts = Aggregation.unwind("products");

        // Stage 3: Group by productId
        GroupOperation group = Aggregation.group("products.productId", "products.productName")
                .sum("products.quantity").as("totalSalesCount")
                .first("products.productName").as("productName");

        // Stage 4: Sort + Limit
        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalSalesCount"));
        LimitOperation limitOp = Aggregation.limit(limit);

        // Stage 5: Project into result shape
        ProjectionOperation project = Aggregation.project()
                .and("_id.productId").as("productId")
                .and("productName").as("productName")
                .and("totalSalesCount").as("totalSalesCount");

        Aggregation agg = Aggregation.newAggregation(match, unwindProducts, group, sort, limitOp, project);

        List<TopSellingProductDTO> top = mongoTemplate.aggregate(agg, Checkout.class, TopSellingProductDTO.class).getMappedResults();

        // Enrich with product info
        for (TopSellingProductDTO dto : top) {
            Product product = mongoTemplate.findById(dto.getProductId(), Product.class);
            if (product != null) {
                dto.setTotalRevenue(product.getPrice() * dto.getTotalSalesCount());

                // Use first variant image if exists
                if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                    ProductVariant firstVariant = mongoTemplate.findById(product.getVariants().get(0).getId(), ProductVariant.class);
                    if (firstVariant != null && firstVariant.getImages() != null && !firstVariant.getImages().isEmpty()) {
                        dto.setImageUrl(firstVariant.getImages().get(0));
                    }

                    // Calculate stock by size
                    Map<String, Integer> sizeStock = new HashMap<>();
                    if (firstVariant.getSizes() != null) {
                        for (SizeVariant sv : firstVariant.getSizes()) {
                            sizeStock.put(sv.getSize(), sv.getStock());
                        }
                        dto.setStockLeftBySize(sizeStock);
                        dto.setTotalStock(sizeStock.values().stream().mapToInt(i -> i).sum());
                    }
                }
            }
        }

        return top;
    }
}