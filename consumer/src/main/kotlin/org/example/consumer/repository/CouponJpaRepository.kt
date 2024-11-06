package org.example.consumer.repository

import org.example.consumer.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponJpaRepository : JpaRepository<Coupon, Long> {}