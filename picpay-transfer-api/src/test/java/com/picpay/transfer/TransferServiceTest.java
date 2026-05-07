package com.picpay.transfer;

import com.picpay.transfer.client.AuthorizerClient;
import com.picpay.transfer.client.NotificationClient;
import com.picpay.transfer.domain.entity.Transfer;
import com.picpay.transfer.domain.entity.User;
import com.picpay.transfer.domain.entity.Wallet;
import com.picpay.transfer.domain.enums.UserType;
import com.picpay.transfer.dto.request.TransferRequest;
import com.picpay.transfer.dto.response.TransferResponse;
import com.picpay.transfer.exception.BusinessException;
import com.picpay.transfer.repository.TransferRepository;
import com.picpay.transfer.repository.UserRepository;
import com.picpay.transfer.repository.WalletRepository;
import com.picpay.transfer.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AuthorizerClient authorizerClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransferService transferService;

    private User payer;
    private User payee;
    private Wallet payerWallet;
    private Wallet payeeWallet;

    @BeforeEach
    void setUp() {
        payer = User.builder()
                .id(1L)
                .fullName("Alice Souza")
                .document("111.111.111-11")
                .email("alice@email.com")
                .password("senha123")
                .userType(UserType.COMMON)
                .build();

        payee = User.builder()
                .id(2L)
                .fullName("Bruno Lima")
                .document("222.222.222-22")
                .email("bruno@email.com")
                .password("senha123")
                .userType(UserType.COMMON)
                .build();

        payerWallet = Wallet.builder()
                .id(1L)
                .balance(new BigDecimal("1000.00"))
                .user(payer)
                .build();

        payeeWallet = Wallet.builder()
                .id(2L)
                .balance(new BigDecimal("500.00"))
                .user(payee)
                .build();
    }

    @Test
    @DisplayName("Deve realizar transferência com sucesso")
    void shouldTransferSuccessfully() {
        TransferRequest request = new TransferRequest(new BigDecimal("100.00"), 1L, 2L);

        Transfer savedTransfer = Transfer.builder()
                .id(1L)
                .value(request.value())
                .payer(payer)
                .payee(payee)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(payerWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(payeeWallet));
        doNothing().when(authorizerClient).authorize();
        when(transferRepository.save(any())).thenReturn(savedTransfer);

        TransferResponse response = transferService.transfer(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("900.00"), payerWallet.getBalance());
        assertEquals(new BigDecimal("600.00"), payeeWallet.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transferRepository).save(any(Transfer.class));
        verify(notificationClient).notify(eq(2L), anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando lojista tenta transferir")
    void shouldThrowWhenMerchantTriesToTransfer() {
        payer.setUserType(UserType.MERCHANT);
        TransferRequest request = new TransferRequest(new BigDecimal("100.00"), 1L, 2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));

        assertThrows(BusinessException.class, () -> transferService.transfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando saldo é insuficiente")
    void shouldThrowWhenInsufficientBalance() {
        TransferRequest request = new TransferRequest(new BigDecimal("9999.00"), 1L, 2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(payee));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(payerWallet));
        when(walletRepository.findByUserId(2L)).thenReturn(Optional.of(payeeWallet));

        assertThrows(BusinessException.class, () -> transferService.transfer(request));
        verify(authorizerClient, never()).authorize();
    }

    @Test
    @DisplayName("Deve lançar exceção ao transferir para si mesmo")
    void shouldThrowWhenTransferringToSelf() {
        TransferRequest request = new TransferRequest(new BigDecimal("100.00"), 1L, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(payer));

        assertThrows(BusinessException.class, () -> transferService.transfer(request));
    }
}
