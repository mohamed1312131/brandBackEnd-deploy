package com.example.hamzabackend.controller;


import com.example.hamzabackend.entity.Note;
import com.example.hamzabackend.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Note> createNote(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam MultipartFile image
    ) {
        try {
            Note note = noteService.createNote(title, description, image);
            return ResponseEntity.ok(note);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        return noteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable String id) {
        noteService.deleteNoteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/active")
    public ResponseEntity<List<Note>> getActiveNotes() {
        return ResponseEntity.ok(noteService.getActiveNotes());
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<Note> enableNote(@PathVariable String id) {
        return ResponseEntity.ok(noteService.enableNote(id));
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Note> disableNote(@PathVariable String id) {
        return ResponseEntity.ok(noteService.disableNote(id));
    }
    @PostMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable String id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Note updatedNote = noteService.updateNote(id, title, description, image);
            return ResponseEntity.ok(updatedNote);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }


}
