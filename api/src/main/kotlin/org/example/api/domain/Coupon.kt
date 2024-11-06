package org.example.api.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id

@Entity
class Coupon (
    @Id @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,
    val userId: Long
)