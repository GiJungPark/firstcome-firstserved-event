package org.example.consumer.consumer

import org.example.consumer.domain.Coupon
import org.example.consumer.domain.FailedEvent
import org.example.consumer.repository.CouponJpaRepository
import org.example.consumer.repository.FailedEventJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CouponCreatedConsumer(
    private val couponJpaRepository: CouponJpaRepository,
    private val failedEventJpaRepository: FailedEventJpaRepository,
) {

    private val logger = LoggerFactory.getLogger(CouponCreatedConsumer::class.java)

    @KafkaListener(topics = ["coupon_create"], groupId = "group_1")
    fun listener(userId: Long) {
        try {
            couponJpaRepository.save(Coupon(userId = userId))
        } catch (e: Exception) {
            logger.error("failed to create coupon:: " + userId)
            failedEventJpaRepository.save(FailedEvent(userId = userId))
        }
    }
}