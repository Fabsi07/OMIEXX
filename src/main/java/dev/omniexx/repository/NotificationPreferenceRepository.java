package dev.omniexx.repository;

import dev.omniexx.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByPlayerIdAndEventType(Long playerId, String eventType);
    List<NotificationPreference> findByPlayerIdAndEnabledTrue(Long playerId);
    List<NotificationPreference> findByEventTypeAndEnabledTrue(String eventType);
}
