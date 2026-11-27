package com.ua.Brickmart.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BrickRequest(
        @NotBlank(message = "назва обов'язкова") String name,
        @NotBlank(message = "виробник обов'язковий") String manufacturer,
        @NotNull(message = "ціна обов'язкова")
        @Positive(message = "ціна має бути додатною") BigDecimal price,
        @Positive(message = "вага має бути додатною") Integer weight,
        @NotBlank(message = "тип цегли обов'язковий") String brickType,
        String photoUrl,
        String description
) {}
