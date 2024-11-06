package org.example.api.service

import org.example.api.producer.CouponCreateProducer
import org.example.api.repository.CouponCountRepository
import org.example.api.repository.CouponJpaRepository
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponCountRepository: CouponCountRepository,
    private val couponCreateProducer: CouponCreateProducer
) {

    fun apply(userId: Long) {
        val couponCount = couponCountRepository.increment()

        if (couponCount > 100) {
            return
        }

        couponCreateProducer.create(userId)
    }
}