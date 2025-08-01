package com.example.hamzabackend.service;

import com.example.hamzabackend.DTO.*;
import com.example.hamzabackend.entity.*;
import com.example.hamzabackend.repository.ProductCategoryRepository;
import com.example.hamzabackend.repository.ProductRepository;
import com.example.hamzabackend.repository.ProductVariantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final ProductVariantRepository variantRepository;

    public ProductService(ProductRepository productRepository,
                          ProductCategoryRepository categoryRepository,
                          ProductVariantRepository variantRepository,
                          CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.variantRepository = variantRepository;
    }



    @Transactional
    public Product createProduct(ProductDetailsDTO dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setOldPrice(dto.getOldPrice());
        product.setAdditionalInfo(dto.getAdditionalInfo());
        product.setSizeGuide(dto.getSizeGuide());

        // --- category handling ---
        if (dto.getNewCategoryName() != null && !dto.getNewCategoryName().isBlank()) {
            ProductCategory cat = new ProductCategory();
            cat.setName(dto.getNewCategoryName());
            categoryRepository.save(cat);
            product.setCategory(cat);
        } else {
            ProductCategory cat = categoryRepository.findById(
                            Objects.requireNonNull(dto.getCategoryId()))
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(cat);
        }


        // initialize empty variants list
        product.setVariants(new ArrayList<>());

        // save and return
        return productRepository.save(product);
    }

    public ProductVariant addVariantToProduct(String productId, ProductVariantUploadDTO variantDTO, MultipartFile[] images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Upload images
        List<String> uploadedImageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            try {
                String url = cloudinaryService.uploadFile(image, "product/" + productId, "image");
                uploadedImageUrls.add(url);
            } catch (IOException e) {
                log.error("Image upload failed", e);
                throw new RuntimeException("Image upload failed", e);
            }
        }

        // Create and save variant
        ProductVariant variant = new ProductVariant();
        variant.setColor(variantDTO.getColor());
        
        // Convert sizes to uppercase before saving
        List<SizeVariant> normalizedSizes = variantDTO.getSizes().stream()
                .map(sizeVariant -> {
                    SizeVariant normalized = new SizeVariant();
                    normalized.setSize(sizeVariant.getSize().toUpperCase());
                    normalized.setStock(sizeVariant.getStock());
                    normalized.setSold(sizeVariant.getSold());
                    return normalized;
                })
                .collect(Collectors.toList());
        variant.setSizes(normalizedSizes);
        variant.setImages(uploadedImageUrls);
        variant.setProductId(productId);

        ProductVariant savedVariant = variantRepository.save(variant);

        // Link variant to product
        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        }
        product.getVariants().add(savedVariant);
        productRepository.save(product);

        log.info("Added variant '{}' to product '{}'. Total variants: {}",
                savedVariant.getColor(), product.getId(), product.getVariants().size());

        return savedVariant;
    }



    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findAll();
    }
    public List<ProductListDTO> getAllProductsForList() {
        List<Product> products = productRepository.findAll();

        return products.stream()
                .map(product -> {
                    List<ProductVariant> variants = variantRepository.findByProductId(product.getId());

                    // Unique sizes from all variants
                    List<String> sizes = variants.stream()
                            .flatMap(v -> v.getSizes().stream())
                            .map(SizeVariant::getSize)
                            .distinct()
                            .collect(Collectors.toList());

                    // Unique colors from all variants
                    List<String> colors = variants.stream()
                            .map(ProductVariant::getColor)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());

                    // Total stock from all variants
                    long stockLeft = variants.stream()
                            .flatMap(v -> v.getSizes().stream())
                            .mapToLong(SizeVariant::getStock)
                            .sum();

                    // Get first image if available
                    List<String> images = variants.stream()
                            .flatMap(v -> v.getImages().stream())
                            .collect(Collectors.toList());

                    String thumbnail = images.isEmpty() ? "" : images.get(0);

                    return new ProductListDTO(
                            product.getId(),
                            product.getTitle(),
                            product.getDescription(),
                            thumbnail,
                            sizes,
                            colors,
                            product.getPrice(),
                            stockLeft,
                            0L,
                            product.getCategory() != null ? product.getCategory().getName() : ""
                    );
                })
                .collect(Collectors.toList());
    }





    public ProductDetailsResponseDTO getProductDetails(String productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductDetailsResponseDTO.VariantDTO> variants = p.getVariants().stream()
                .map(v -> new ProductDetailsResponseDTO.VariantDTO(
                        v.getId(),                 // pass variantId
                        v.getColor(),
                        v.getImages(),
                        v.getSizes().stream()
                                .map(s -> new ProductDetailsResponseDTO.SizeStockDTO(
                                        s.getSize(), 
                                        s.getStock(),           // Current remaining stock
                                        s.getSold(),            // Total sold quantity
                                        s.getStock() + s.getSold() // Original total stock
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());



        // load all categories
        List<CategoryDTO> allCats = categoryRepository.findAll().stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName()))
                .collect(Collectors.toList());

        return new ProductDetailsResponseDTO(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                allCats,
                p.getPrice(),
                p.getOldPrice(),
                p.getAdditionalInfo(),
                p.getSizeGuide(),
                variants
        );
    }
    @Transactional
    public Product updateProduct(String productId, ProductDetailsDTO dto, MultipartFile sizeGuideImage) {
        // Handle size guide image upload if provided
        if (sizeGuideImage != null && !sizeGuideImage.isEmpty()) {
            try {
                String sizeGuideUrl = cloudinaryService.uploadFile(sizeGuideImage, "product/" + productId + "/size-guide", "image");
                dto.setSizeGuide(sizeGuideUrl);
            } catch (IOException e) {
                log.error("Failed to upload size guide image", e);
                throw new RuntimeException("Size guide image upload failed", e);
            }
        }
        
        return updateProduct(productId, dto);
    }
    
    @Transactional
    public Product updateProduct(String productId, ProductDetailsDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update core fields
        product.setTitle(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setSizeGuide(dto.getSizeGuide()); // 👈
        product.setOldPrice(dto.getOldPrice());
        product.setAdditionalInfo(dto.getAdditionalInfo());
        product.setSizeGuide(dto.getSizeGuide());

        // Category logic
        if (dto.getNewCategoryName() != null && !dto.getNewCategoryName().isBlank()) {
            ProductCategory cat = new ProductCategory();
            cat.setName(dto.getNewCategoryName());
            categoryRepository.save(cat);
            product.setCategory(cat);
        } else {
            ProductCategory cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(cat);
        }

        // ✅ Handle deleted variants
        if (dto.getVariantsToDelete() != null && !dto.getVariantsToDelete().isEmpty()) {
            for (String variantId : dto.getVariantsToDelete()) {
                variantRepository.deleteById(variantId);
            }
            if (product.getVariants() != null) {
                product.getVariants().removeIf(v -> dto.getVariantsToDelete().contains(v.getId()));
            }
        }

        return productRepository.save(product);
    }


    /**
     * Update an existing variant inside a product.
     */
    @Transactional
    public ProductVariant updateProductVariant(
            String productId,
            String variantId,
            ProductVariantUpdateDTO dto,
            MultipartFile[] newImages
    ) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (!variant.getProductId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to the given product");
        }

        // Remove images
        if (dto.getImagesToRemove() != null) {
            variant.getImages().removeAll(dto.getImagesToRemove());
        }

        // Upload new images
        if (newImages != null) {
            for (MultipartFile file : newImages) {
                try {
                    String url = cloudinaryService.uploadFile(file, "product/" + productId + "/variant/" + variantId, "image");
                    variant.getImages().add(url);
                } catch (IOException e) {
                    log.error("Failed to upload image: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("Image upload failed", e);
                }
            }
        }

        // Update color and sizes
        variant.setColor(dto.getColor());
        List<SizeVariant> sizes = dto.getSizes().stream()
                .map(d -> {
                    SizeVariant sv = new SizeVariant();
                    sv.setSize(d.getSize().toUpperCase());
                    sv.setStock(d.getStock());
                    return sv;
                })
                .collect(Collectors.toList());
        variant.setSizes(sizes);

        return variantRepository.save(variant);
    }
    @Transactional
    public void deleteVariant(String productId, String variantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        if (!variant.getProductId().equals(productId)) {
            throw new RuntimeException("Variant does not belong to product");
        }

        // Remove reference from product
        product.getVariants().removeIf(v -> v.getId().equals(variantId));
        productRepository.save(product);

        // Delete variant
        variantRepository.deleteById(variantId);
    }
    @Transactional
    public void deleteProduct(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // First delete all variants
        if (product.getVariants() != null) {
            for (ProductVariant variant : product.getVariants()) {
                variantRepository.deleteById(variant.getId());
            }
        }

        // Then delete the product (category remains untouched)
        productRepository.deleteById(productId);
    }
    public List<ProductListDTO> getLatestProducts(int count) {
        List<Product> products = productRepository.findAllByOrderByCreatedAtDesc(); // you'll need to add this repo method

        return products.stream()
                .limit(count)
                .map(product -> {
                    List<ProductVariant> variants = variantRepository.findByProductId(product.getId());
                    List<String> sizes = variants.stream().flatMap(v -> v.getSizes().stream()).map(SizeVariant::getSize).distinct().toList();
                    List<String> colors = variants.stream().map(ProductVariant::getColor).filter(Objects::nonNull).distinct().toList();
                    long stockLeft = variants.stream().flatMap(v -> v.getSizes().stream()).mapToLong(SizeVariant::getStock).sum();
                    List<String> images = variants.stream().flatMap(v -> v.getImages().stream()).toList();
                    String thumbnail = images.isEmpty() ? "" : images.get(0);

                    return new ProductListDTO(
                            product.getId(),
                            product.getTitle(),
                            product.getDescription(),
                            thumbnail,
                            sizes,
                            colors,
                            product.getPrice(),
                            stockLeft,
                            0L,
                            product.getCategory() != null ? product.getCategory().getName() : ""
                    );
                })
                .collect(Collectors.toList());
    }







}
