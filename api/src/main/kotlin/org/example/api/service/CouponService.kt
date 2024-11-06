package org.example.api.service

import org.example.api.domain.Coupon
import org.example.api.repository.CouponJpaRepository
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponJpaRepository: CouponJpaRepository
) {
    fun apply(userId: Long) {
        val couponCount = couponJpaRepository.count()

        if (couponCount > 100) {
            return
        }

        couponJpaRepository.save(Coupon(userId = userId))
    }
}