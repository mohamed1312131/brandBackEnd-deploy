package com.example.hamzabackend.service;

import com.example.hamzabackend.DTO.ClientInquiryRequest;
import com.example.hamzabackend.entity.ClientInquiry;
import com.example.hamzabackend.exception.BadRequestException;
import com.example.hamzabackend.repository.ClientInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ClientInquiryService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int SHORT_TEXT_MAX = 120;
    private static final int LONG_TEXT_MAX = 2000;

    private final ClientInquiryRepository repository;

    /**
     * Validates and stores a public submission. Returns empty when the honeypot was filled,
     * so bots get a normal-looking response without anything being persisted.
     */
    public Optional<ClientInquiry> submit(ClientInquiryRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            return Optional.empty();
        }

        String name = required(request.getName(), "Name", SHORT_TEXT_MAX);
        String email = required(request.getEmail(), "Email", 254).toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("Email is invalid");
        }

        // Allow "yesterday" so visitors in time zones behind the server aren't rejected.
        LocalDate earliest = LocalDate.now().minusDays(1);
        validateNotPast(request.getEventDate(), earliest, "Event date");
        validateNotPast(request.getAppointmentDatePreference(), earliest, "Appointment date preference");

        boolean withStylist = Boolean.TRUE.equals(request.getWorkingWithStylist());
        List<ClientInquiry.AppointmentType> appointmentTypes = request.getAppointmentTypes() == null
                ? List.of()
                : request.getAppointmentTypes().stream().filter(Objects::nonNull).distinct().sorted().toList();

        Instant now = Instant.now();
        ClientInquiry inquiry = ClientInquiry.builder()
                .name(name)
                .email(email)
                .phone(optional(request.getPhone(), "Phone number", 40))
                .preferredContactMethod(request.getPreferredContactMethod())
                .eventDate(request.getEventDate())
                .appointmentDatePreference(request.getAppointmentDatePreference())
                .appointmentTypes(appointmentTypes)
                .interest(request.getInterest())
                .stylesOfInterest(optional(request.getStylesOfInterest(), "Styles of interest", LONG_TEXT_MAX))
                .discoverySource(optional(request.getDiscoverySource(), "How did you discover the brand", LONG_TEXT_MAX))
                .workingWithStylist(request.getWorkingWithStylist())
                .stylistName(withStylist ? optional(request.getStylistName(), "Stylist", SHORT_TEXT_MAX) : null)
                .status(ClientInquiry.Status.NEW)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return Optional.of(repository.save(inquiry));
    }

    public List<ClientInquiry> getAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<ClientInquiry> updateStatus(String id, ClientInquiry.Status status) {
        if (status == null) {
            throw new BadRequestException("Status is required");
        }
        return repository.findById(id).map(inquiry -> {
            inquiry.setStatus(status);
            inquiry.setUpdatedAt(Instant.now());
            return repository.save(inquiry);
        });
    }

    public boolean delete(String id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    private static String required(String value, String field, int maxLength) {
        String trimmed = optional(value, field, maxLength);
        if (trimmed == null) {
            throw new BadRequestException(field + " is required");
        }
        return trimmed;
    }

    private static String optional(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BadRequestException(field + " must be at most " + maxLength + " characters");
        }
        return trimmed;
    }

    private static void validateNotPast(LocalDate date, LocalDate earliest, String field) {
        if (date != null && date.isBefore(earliest)) {
            throw new BadRequestException(field + " cannot be in the past");
        }
    }
}
