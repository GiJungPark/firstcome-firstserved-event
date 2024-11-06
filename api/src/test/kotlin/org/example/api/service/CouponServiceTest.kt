package org.example.api.service

import org.assertj.core.api.Assertions.assertThat
import org.example.api.repository.CouponCountRepository
import org.example.api.repository.CouponJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponJpaRepository

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @AfterEach
    fun tearDown() {
        couponRepository.deleteAllInBatch()
        redisTemplate.delete(redisTemplate.keys("coupon_count"))
        redisTemplate.delete(redisTemplate.keys("applied_user"))

        while (redisTemplate.keys("coupon_count").isNotEmpty()
            || redisTemplate.keys("applied_user").isNotEmpty()) {
            Thread.sleep(100)
        }
    }

    @DisplayName("쿠폰을 발행한다.")
    @Test
    fun apply() {
        // given
        val userId = 1L

        // when
        couponService.apply(userId)

        Thread.sleep(10000)

        // then
        val count = couponRepository.count()

        assertThat(count).isEqualTo(1)
    }

    @DisplayName("1000번의 요청이 있는 경우, 100개의 쿠폰만 발행한다.")
    @Test
    fun applyMultipleConcurrentCall() {
        // given
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0..threadCount) {
            val userId = i.toLong()
            executorService.submit({
                try {
                    couponService.apply(userId)
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await()

        Thread.sleep(10000)

        // then
        val count = couponRepository.count()

        assertThat(count).isEqualTo(100)
    }

    @DisplayName("쿠폰은 1인당 1개만 발행한다.")
    @Test
    fun applyDuplicateUser() {
        // given
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 0..threadCount) {
            executorService.submit({
                try {
                    couponService.apply(1L)
                } finally {
                    latch.countDown()
                }
            })
        }

        latch.await()

        Thread.sleep(10000)

        // then
        val count = couponRepository.count()

        assertThat(count).isEqualTo(1)
    }
}