package com.gamesphere.notifications.repository;

import com.gamesphere.notifications.domain.NotificationPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationPreferencesRepository extends JpaRepository<NotificationPreferences, Long> {
}
