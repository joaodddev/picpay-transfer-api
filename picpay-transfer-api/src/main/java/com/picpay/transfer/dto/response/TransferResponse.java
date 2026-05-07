package com.picpay.transfer.dto.response;

import com.picpay.transfer.domain.entity.Transfer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(
        Long id,
        BigDecimal value,
        Long payerId,
        Long payeeId,
        LocalDateTime createdAt
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getValue(),
                transfer.getPayer().getId(),
                transfer.getPayee().getId(),
                transfer.getCreatedAt()
        );
    }
}
