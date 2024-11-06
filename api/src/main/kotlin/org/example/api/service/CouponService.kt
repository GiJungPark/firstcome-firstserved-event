package org.example.api.service

import org.example.api.producer.CouponCreateProducer
import org.example.api.repository.AppliedUserRepository
import org.example.api.repository.CouponCountRepository
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponCountRepository: CouponCountRepository,
    private val appliedUserRepository: AppliedUserRepository,
    private val couponCreateProducer: CouponCreateProducer
) {

    fun apply(userId: Long) {

        val apply = appliedUserRepository.add(userId)

        if (apply != 1L) {
            return
        }

        val couponCount = couponCountRepository.increment()

        if (couponCount > 100) {
            return
        }

        couponCreateProducer.create(userId)
    }
}