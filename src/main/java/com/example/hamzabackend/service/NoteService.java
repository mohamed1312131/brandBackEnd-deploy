package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.Note;
import com.example.hamzabackend.exception.BadRequestException;
import com.example.hamzabackend.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class NoteService {

    // Site-relative paths ("/shop", not "//evil.com") or absolute https links; anything else (javascript:, data:) is rejected.
    private static final Pattern SAFE_URL = Pattern.compile("^(/(?![/\\\\])\\S*|https://\\S+)$");
    private static final int MAX_BUTTON_TEXT = 40;
    private static final int MAX_BUTTON_URL = 500;

    public record ButtonSettings(String buttonType, String buttonText, String buttonUrl, String imagePosition) {}

    private final NoteRepository noteRepository;
    private final CloudinaryService cloudinaryService;

    public NoteService(NoteRepository noteRepository, CloudinaryService cloudinaryService) {
        this.noteRepository = noteRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Note createNote(String title, String description, MultipartFile imageFile, ButtonSettings settings) throws IOException {
        Note note = new Note();
        applySettings(note, settings);
        String imageUrl = cloudinaryService.uploadImage(imageFile);

        note.setTitle(title);
        note.setDescription(description);
        note.setImageUrl(imageUrl);
        note.setCreatedAt(Instant.now());

        return noteRepository.save(note);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Optional<Note> getNoteById(String id) {
        return noteRepository.findById(id);
    }

    public void deleteNoteById(String id) {
        noteRepository.deleteById(id);
    }
    public List<Note> getActiveNotes() {
        return noteRepository.findByStatusTrue();
    }

    public Note enableNote(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        note.setStatus(true);
        return noteRepository.save(note);
    }

    public Note disableNote(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        note.setStatus(false);
        return noteRepository.save(note);
    }
    public Note updateNote(String id, String title, String description, MultipartFile image, ButtonSettings settings) throws IOException {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));
        // Older admin builds don't send the settings; keep what the note already has.
        if (settings.buttonType() != null) {
            applySettings(note, settings);
        }

        note.setTitle(title);
        note.setDescription(description);

        // A note always needs an image, so no upload means keep the current one.
        if (image != null && !image.isEmpty()) {
            note.setImageUrl(cloudinaryService.uploadImage(image));
        }

        return noteRepository.save(note);
    }

    private static void applySettings(Note note, ButtonSettings settings) {
        Note.ButtonType type = parse(Note.ButtonType.class, settings.buttonType(), Note.ButtonType.SHOP, "buttonType");
        Note.ImagePosition position = parse(Note.ImagePosition.class, settings.imagePosition(), Note.ImagePosition.LEFT, "imagePosition");

        String text = trimToNull(settings.buttonText());
        if (text != null && text.length() > MAX_BUTTON_TEXT) {
            throw new BadRequestException("buttonText must be at most " + MAX_BUTTON_TEXT + " characters");
        }

        String url = null;
        if (type == Note.ButtonType.CUSTOM) {
            url = trimToNull(settings.buttonUrl());
            if (url == null) {
                throw new BadRequestException("buttonUrl is required for a custom button");
            }
            if (url.length() > MAX_BUTTON_URL || !SAFE_URL.matcher(url).matches()) {
                throw new BadRequestException("buttonUrl must start with / or https://");
            }
        }

        note.setButtonType(type);
        note.setButtonText(type == Note.ButtonType.NONE ? null : text);
        note.setButtonUrl(url);
        note.setImagePosition(position);
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, E fallback, String field) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid " + field);
        }
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
