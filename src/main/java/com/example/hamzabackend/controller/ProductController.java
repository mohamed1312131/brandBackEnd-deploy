package com.example.hamzabackend.controller;

import com.example.hamzabackend.DTO.*;
import com.example.hamzabackend.entity.Product;
import com.example.hamzabackend.entity.ProductCategory;
import com.example.hamzabackend.entity.ProductVariant;
import com.example.hamzabackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ... createProduct, uploadVariant, and other GET methods remain the same ...
    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody ProductDetailsDTO dto) {
        Product product = productService.createProduct(dto);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/{productId}/variant")
    public ResponseEntity<ProductVariant> uploadVariant(
            @PathVariable String productId,
            @RequestPart("variant") ProductVariantUploadDTO variantDTO,
            @RequestPart(name = "images", required = false) MultipartFile[] images
    ) {
        ProductVariant createdVariant = productService.addVariantToProduct(productId, variantDTO, images != null ? images : new MultipartFile[0]);
        return ResponseEntity.ok(createdVariant);
    }

    @GetMapping("/getAllCat")
    public ResponseEntity<List<ProductCategory>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }
    @GetMapping
    public ResponseEntity<List<ProductListDTO>> listAllProducts() {
        List<ProductListDTO> dtos = productService.getAllProductsForList();
        return ResponseEntity.ok(dtos);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailsResponseDTO> getDetails(@PathVariable String id) {
        ProductDetailsResponseDTO dto = productService.getProductDetails(id);
        return ResponseEntity.ok(dto);
    }

    // ===================================================================
    // ✨ START: REPLACEMENT FOR BOTH OLD UPDATE METHODS
    // ===================================================================

    /**
     * Handles updating a product's details and optionally its size guide image.
     * This single endpoint correctly consumes multipart/form-data from the Angular client.
     */
    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestPart("productData") ProductDetailsDTO dto, // ✅ Matches the key from Angular
            @RequestPart(name = "sizeGuideImage", required = false) MultipartFile sizeGuideImage
    ) {
        // The existing service method already handles this logic perfectly.
        Product updated = productService.updateProduct(id, dto, sizeGuideImage);
        return ResponseEntity.ok(updated);
    }

    // ===================================================================
    // ✨ END: REPLACEMENT
    // ===================================================================


    @PutMapping(value = "/{productId}/variant/{variantId}", consumes = { "multipart/form-data" })
    public ResponseEntity<ProductVariant> updateVariant(
            @PathVariable String productId,
            @PathVariable String variantId,
            @RequestPart("variant") ProductVariantUpdateDTO dto,
            @RequestPart(name = "newImages", required = false) MultipartFile[] newImages
    ) {
        ProductVariant updatedVariant = productService.updateProductVariant(
                productId, variantId, dto, newImages
        );
        return ResponseEntity.ok(updatedVariant);
    }

    // ... delete methods and other endpoints remain the same ...
    @DeleteMapping("/{productId}/variant/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable String productId,
            @PathVariable String variantId
    ) {
        System.out.println("DELETE called for variant: " + variantId);
        productService.deleteVariant(productId, variantId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/latest")
    public ResponseEntity<List<ProductListDTO>> getLatestProducts() {
        return ResponseEntity.ok(productService.getLatestProducts(6));
    }
}