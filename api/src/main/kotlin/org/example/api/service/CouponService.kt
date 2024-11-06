package org.example.api.service

import org.example.api.domain.Coupon
import org.example.api.repository.CouponCountRepository
import org.example.api.repository.CouponJpaRepository
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponJpaRepository: CouponJpaRepository,
    private val couponCountRepository: CouponCountRepository
) {

    fun apply(userId: Long) {
        val couponCount = couponCountRepository.increment()

        if (couponCount > 100) {
            return
        }

        couponJpaRepository.save(Coupon(userId = userId))
    }
}