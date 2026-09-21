package com.nguyenthongnhat.backend_tttn.controller;

import com.nguyenthongnhat.backend_tttn.dto.ContactDTO;
import com.nguyenthongnhat.backend_tttn.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ContactController {

    private final ContactService contactService;

    // Public endpoint for users to submit contact form
    @PostMapping
    public ResponseEntity<ContactDTO> submitContact(@Valid @RequestBody ContactDTO contactDTO) {
        return ResponseEntity.ok(contactService.createContact(contactDTO));
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<Page<ContactDTO>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(contactService.searchContacts(search, pageable));
        }

        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            return ResponseEntity.ok(contactService.getContactsByStatus(status, pageable));
        }

        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ContactDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(contactService.updateStatus(id, status));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ContactDTO> replyContact(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String replyMessage = body.get("replyMessage");
        return ResponseEntity.ok(contactService.replyContact(id, replyMessage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok().build();
    }
}
