package com.nguyenthongnhat.backend_tttn.repository;

import com.nguyenthongnhat.backend_tttn.entity.ChatbotMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    @Query("SELECT m.sessionId, COUNT(m.id) FROM ChatbotMessage m GROUP BY m.sessionId")
    List<Object[]> countChatsPerSession();

    @Query("SELECT m.productSuggestedId, COUNT(m.id) as cnt FROM ChatbotMessage m " +
           "WHERE m.productSuggestedId IS NOT NULL " +
           "GROUP BY m.productSuggestedId " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopSuggestedProducts(Pageable pageable);

    @Query("SELECT m.message, COUNT(m.id) as cnt FROM ChatbotMessage m " +
           "WHERE m.sender = 'USER' " +
           "GROUP BY m.message " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopQuestions(Pageable pageable);
}
