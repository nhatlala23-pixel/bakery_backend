package com.nguyenthongnhat.backend_tttn.service.impl;

import com.nguyenthongnhat.backend_tttn.dto.ContactDTO;
import com.nguyenthongnhat.backend_tttn.entity.Contact;
import com.nguyenthongnhat.backend_tttn.repository.ContactRepository;
import com.nguyenthongnhat.backend_tttn.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final JavaMailSender mailSender;

    private ContactDTO mapToDTO(Contact contact) {
        ContactDTO dto = new ContactDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setEmail(contact.getEmail());
        dto.setPhone(contact.getPhone());
        dto.setSubject(contact.getSubject());
        dto.setMessage(contact.getMessage());
        dto.setStatus(contact.getStatus());
        dto.setAdminReply(contact.getAdminReply());
        dto.setRepliedAt(contact.getRepliedAt());
        dto.setCreatedAt(contact.getCreatedAt());
        return dto;
    }

    private Contact mapToEntity(ContactDTO dto) {
        Contact contact = new Contact();
        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setSubject(dto.getSubject());
        contact.setMessage(dto.getMessage());
        // Status has default "PENDING"
        return contact;
    }

    @Override
    public ContactDTO createContact(ContactDTO contactDTO) {
        Contact contact = mapToEntity(contactDTO);
        Contact savedContact = contactRepository.save(contact);
        return mapToDTO(savedContact);
    }

    @Override
    public Page<ContactDTO> getAllContacts(Pageable pageable) {
        return contactRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    public Page<ContactDTO> getContactsByStatus(String status, Pageable pageable) {
        return contactRepository.findByStatus(status, pageable).map(this::mapToDTO);
    }

    @Override
    public Page<ContactDTO> searchContacts(String keyword, Pageable pageable) {
        return contactRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::mapToDTO);
    }

    @Override
    public ContactDTO getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ"));
        return mapToDTO(contact);
    }

    @Override
    public ContactDTO updateStatus(Long id, String status) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ"));
        contact.setStatus(status);
        Contact updatedContact = contactRepository.save(contact);
        return mapToDTO(updatedContact);
    }

    @Override
    public ContactDTO replyContact(Long id, String replyMessage) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên hệ"));
        
        // Lưu phản hồi
        contact.setAdminReply(replyMessage);
        contact.setRepliedAt(LocalDateTime.now());
        contact.setStatus("REPLIED");
        Contact saved = contactRepository.save(contact);
        
        // Gửi email cho khách hàng
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(contact.getEmail());
            message.setSubject("Phản hồi từ Techno về yêu cầu hỗ trợ: " + contact.getSubject());
            
            String emailContent = "Xin chào " + contact.getName() + ",\n\n"
                    + "Cảm ơn bạn đã liên hệ với Techno.\n"
                    + "Dưới đây là nội dung phản hồi cho yêu cầu hỗ trợ của bạn:\n\n"
                    + "--------------------------------------------------------\n"
                    + replyMessage + "\n"
                    + "--------------------------------------------------------\n\n"
                    + "Nếu có thêm câu hỏi nào, vui lòng liên hệ lại với chúng tôi.\n\n"
                    + "Trân trọng,\nĐội ngũ CSKH Techno.";
            
            message.setText(emailContent);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email phản hồi: " + e.getMessage());
            // Có thể chọn throw exception hoặc chỉ log lỗi để không làm gián đoạn luồng chính
        }
        
        return mapToDTO(saved);
    }

    @Override
    public void deleteContact(Long id) {
        if (!contactRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy liên hệ");
        }
        contactRepository.deleteById(id);
    }
}
