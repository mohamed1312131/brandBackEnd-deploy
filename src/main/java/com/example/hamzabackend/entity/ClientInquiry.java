package com.example.hamzabackend.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("client_inquiries")
public class ClientInquiry {

    @Id
    private String id;

    private String name;
    private String email;
    private String phone;
    private ContactMethod preferredContactMethod;
    private LocalDate eventDate;
    private LocalDate appointmentDatePreference;
    private List<AppointmentType> appointmentTypes;
    private Interest interest;
    private String stylesOfInterest;
    private String discoverySource;
    private Boolean workingWithStylist;
    private String stylistName;

    private Status status;

    @Indexed
    private Instant createdAt;
    private Instant updatedAt;

    public enum ContactMethod { EMAIL, WHATSAPP, PHONE_CALL }

    public enum AppointmentType { IN_PERSON, VIRTUAL }

    public enum Interest { STANDARD_SIZE, CUSTOMIZED_DRESS, SAMPLE_PURCHASE }

    public enum Status { NEW, CONTACTED, CLOSED }
}
