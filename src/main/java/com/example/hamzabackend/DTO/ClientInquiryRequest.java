package com.example.hamzabackend.DTO;

import com.example.hamzabackend.entity.ClientInquiry;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Public submission payload: deliberately excludes id/status/timestamps so clients can't set them.
@Data
public class ClientInquiryRequest {
    private String name;
    private String email;
    private String phone;
    private ClientInquiry.ContactMethod preferredContactMethod;
    private LocalDate eventDate;
    private LocalDate appointmentDatePreference;
    private List<ClientInquiry.AppointmentType> appointmentTypes;
    private ClientInquiry.Interest interest;
    private String stylesOfInterest;
    private String discoverySource;
    private Boolean workingWithStylist;
    private String stylistName;

    // Honeypot: hidden in the UI, so only bots fill it.
    private String website;
}
