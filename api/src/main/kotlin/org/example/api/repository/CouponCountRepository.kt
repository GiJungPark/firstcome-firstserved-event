package org.example.api.repository

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository

@Repository
class CouponCountRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {
    fun increment(): Long {
        return redisTemplate.opsForValue().increment("coupon_count")!!
    }
}