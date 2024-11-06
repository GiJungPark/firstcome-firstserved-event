package org.example.consumer.consumer

import org.example.consumer.domain.Coupon
import org.example.consumer.repository.CouponJpaRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CouponCreatedConsumer(
    private val couponJpaRepository: CouponJpaRepository
) {

    @KafkaListener(topics = ["coupon_create"], groupId = "group_1")
    fun listener(userId: Long) {
        couponJpaRepository.save(Coupon(userId = userId))
    }
}