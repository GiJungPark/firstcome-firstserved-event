package org.example.consumer.repository

import org.example.consumer.domain.FailedEvent
import org.springframework.data.jpa.repository.JpaRepository

interface FailedEventJpaRepository : JpaRepository<FailedEvent, Long> {}