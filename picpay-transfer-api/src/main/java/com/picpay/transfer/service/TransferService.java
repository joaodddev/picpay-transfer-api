package com.picpay.transfer.service;

import com.picpay.transfer.client.AuthorizerClient;
import com.picpay.transfer.client.NotificationClient;
import com.picpay.transfer.domain.entity.Transfer;
import com.picpay.transfer.domain.entity.User;
import com.picpay.transfer.domain.entity.Wallet;
import com.picpay.transfer.dto.request.TransferRequest;
import com.picpay.transfer.dto.response.TransferResponse;
import com.picpay.transfer.exception.BusinessException;
import com.picpay.transfer.repository.TransferRepository;
import com.picpay.transfer.repository.UserRepository;
import com.picpay.transfer.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransferRepository transferRepository;
    private final AuthorizerClient authorizerClient;
    private final NotificationClient notificationClient;

    public TransferService(UserRepository userRepository,
                           WalletRepository walletRepository,
                           TransferRepository transferRepository,
                           AuthorizerClient authorizerClient,
                           NotificationClient notificationClient) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transferRepository = transferRepository;
        this.authorizerClient = authorizerClient;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // 1. Busca os usuários
        User payer = findUserById(request.payer());
        User payee = findUserById(request.payee());

        // 2. Lojista não pode enviar dinheiro
        if (payer.isMerchant()) {
            throw new BusinessException(
                    "Lojistas não podem realizar transferências.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // 3. Payer e payee não podem ser o mesmo usuário
        if (payer.getId().equals(payee.getId())) {
            throw new BusinessException(
                    "Não é possível transferir para si mesmo.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // 4. Busca as carteiras
        Wallet payerWallet = findWalletByUser(payer.getId());
        Wallet payeeWallet = findWalletByUser(payee.getId());

        // 5. Verifica saldo suficiente
        if (!payerWallet.hasSufficientBalance(request.value())) {
            throw new BusinessException(
                    "Saldo insuficiente para realizar a transferência.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // 6. Consulta o serviço autorizador externo
        authorizerClient.authorize();

        // 7. Realiza a transferência (débito e crédito)
        payerWallet.debit(request.value());
        payeeWallet.credit(request.value());

        walletRepository.save(payerWallet);
        walletRepository.save(payeeWallet);

        // 8. Persiste a transferência
        Transfer transfer = Transfer.builder()
                .value(request.value())
                .payer(payer)
                .payee(payee)
                .build();

        Transfer saved = transferRepository.save(transfer);

        // 9. Envia notificação (best-effort: não reverte se falhar)
        notificationClient.notify(
                payee.getId(),
                String.format("Você recebeu R$ %.2f de %s.", request.value(), payer.getFullName())
        );

        return TransferResponse.from(saved);
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "Usuário com id " + id + " não encontrado.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private Wallet findWalletByUser(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(
                        "Carteira do usuário " + userId + " não encontrada.",
                        HttpStatus.NOT_FOUND
                ));
    }
}
