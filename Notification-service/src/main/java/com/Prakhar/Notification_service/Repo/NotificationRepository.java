package com.Prakhar.Notification_service.Repo;

import com.Prakhar.Notification_service.Entity.NotificationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntry, Long> {


}