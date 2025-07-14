package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.Note;
import com.example.hamzabackend.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final CloudinaryService cloudinaryService;

    public NoteService(NoteRepository noteRepository, CloudinaryService cloudinaryService) {
        this.noteRepository = noteRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Note createNote(String title, String description, MultipartFile imageFile) throws IOException {
        String imageUrl = cloudinaryService.uploadImage(imageFile);

        Note note = new Note();
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
    public Note updateNote(String id, String title, String description, MultipartFile image) throws IOException {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

        note.setTitle(title);
        note.setDescription(description);

        if (image != null && !image.isEmpty()) {
            // Upload new image
            String newImageUrl = cloudinaryService.uploadImage(image);
            note.setImageUrl(newImageUrl);
        } else if (image == null) {
            // If explicitly null and image was removed, clear the image
            note.setImageUrl(null);
        }
        // else: keep the current imageUrl as is

        return noteRepository.save(note);
    }



}
