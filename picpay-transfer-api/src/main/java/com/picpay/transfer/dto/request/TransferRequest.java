package com.picpay.transfer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(

        @NotNull(message = "O valor é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal value,

        @NotNull(message = "O pagador é obrigatório")
        Long payer,

        @NotNull(message = "O recebedor é obrigatório")
        Long payee
) {}
