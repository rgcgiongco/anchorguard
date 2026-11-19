package com.anchorguard.api.admin

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SpaFallbackController {

    private val index: Resource = ClassPathResource("static/index.html")

    @GetMapping(value = ["/"])
    fun root(): Resource = index

    @GetMapping(value = ["/{path:[^\\.]*}"])
    fun fallback(): Resource = index
}