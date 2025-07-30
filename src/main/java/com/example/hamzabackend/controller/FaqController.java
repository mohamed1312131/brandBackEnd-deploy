package com.example.hamzabackend.controller;

import com.example.hamzabackend.entity.Faq;
import com.example.hamzabackend.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FaqController {

    private final FaqService faqService;

    @GetMapping
    public List<Faq> getAllFaqs() {
        return faqService.getAllFaqs();
    }

    @GetMapping("/{id}")
    public Faq getFaqById(@PathVariable String id) {
        return faqService.getFaqById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found"));
    }

    @PostMapping
    public Faq createFaq(@RequestBody Faq faq) {
        return faqService.addFaq(faq);
    }

    @PostMapping("/batch")
    public List<Faq> createMultipleFaqs(@RequestBody List<Faq> faqs) {
        return faqService.addMultipleFaqs(faqs);
    }

    @PutMapping("/{id}")
    public Faq updateFaq(@PathVariable String id, @RequestBody Faq faq) {
        return faqService.updateFaq(id, faq);
    }

    @DeleteMapping("/{id}")
    public void deleteFaq(@PathVariable String id) {
        faqService.deleteFaq(id);
    }

    @DeleteMapping("/batch")
    public void deleteMultipleFaqs(@RequestBody List<String> ids) {
        faqService.deleteMultipleFaqs(ids);
    }
}
