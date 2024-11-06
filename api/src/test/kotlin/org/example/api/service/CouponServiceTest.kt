package org.example.api.service

import org.assertj.core.api.Assertions.assertThat
import org.example.api.repository.CouponJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private lateinit var couponService: CouponService

    @Autowired
    private lateinit var couponRepository: CouponJpaRepository

    @AfterEach
    fun tearDown() {
        couponRepository.deleteAllInBatch()
    }

    @DisplayName("쿠폰을 발행한다.")
    @Test
    fun apply() {
        // given
        val userId = 1L

        // when
        couponService.apply(userId)

        // then
        val count = couponRepository.count()

        assertThat(count).isEqualTo(1)
    }
}