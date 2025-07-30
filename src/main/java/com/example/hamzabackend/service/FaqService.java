package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.Faq;
import com.example.hamzabackend.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    public List<Faq> getAllFaqs() {
        return faqRepository.findAll();
    }

    public Optional<Faq> getFaqById(String id) {
        return faqRepository.findById(id);
    }

    public Faq addFaq(Faq faq) {
        return faqRepository.save(faq);
    }

    public List<Faq> addMultipleFaqs(List<Faq> faqs) {
        return faqRepository.saveAll(faqs);
    }

    public Faq updateFaq(String id, Faq updatedFaq) {
        return faqRepository.findById(id).map(faq -> {
            faq.setQuestion(updatedFaq.getQuestion());
            faq.setAnswer(updatedFaq.getAnswer());
            return faqRepository.save(faq);
        }).orElseThrow(() -> new RuntimeException("FAQ not found"));
    }

    public void deleteFaq(String id) {
        faqRepository.deleteById(id);
    }

    public void deleteMultipleFaqs(List<String> ids) {
        ids.forEach(faqRepository::deleteById);
    }
}
