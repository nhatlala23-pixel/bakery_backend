package com.nguyenthongnhat.backend_tttn.service;

import com.nguyenthongnhat.backend_tttn.dto.ContactDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactService {
    ContactDTO createContact(ContactDTO contactDTO);
    Page<ContactDTO> getAllContacts(Pageable pageable);
    Page<ContactDTO> getContactsByStatus(String status, Pageable pageable);
    Page<ContactDTO> searchContacts(String keyword, Pageable pageable);
    ContactDTO getContactById(Long id);
    ContactDTO updateStatus(Long id, String status);
    ContactDTO replyContact(Long id, String replyMessage);
    void deleteContact(Long id);
}
