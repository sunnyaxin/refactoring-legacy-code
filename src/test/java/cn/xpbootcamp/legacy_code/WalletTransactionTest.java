package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletTransactionTest {
    private static RedisDistributedLock lock;

    @BeforeAll
    static void beforeAll() {
        lock = mock(RedisDistributedLock.class);
        new MockUp<RedisDistributedLock>() {
            @Mock
            public RedisDistributedLock getSingletonInstance() {
                return lock;
            }
        };
    }

    @Test
    void should_execute_false_when_lock_failed() throws InvalidTransactionException {
        WalletTransaction transaction = new WalletTransaction("t_test_id", 1L, 1L);
        when(lock.lock("t_test_id")).thenReturn(false);

        boolean result = transaction.execute(any(WalletService.class));

        assertThat(result).isFalse();
    }

    @Test
    void should_throw_exception_when_buyerId_is_null() {
        WalletTransaction transaction = new WalletTransaction("test_id", null, 1L);

        assertThatThrownBy(() -> transaction.execute(any(WalletService.class))).hasMessage("This is an invalid transaction");
    }

    @Test
    void should_throw_exception_when_sellerId_is_null() {
        WalletTransaction transaction = new WalletTransaction("test_id", 1L, null);

        assertThatThrownBy(() -> transaction.execute(any(WalletService.class))).hasMessage("This is an invalid transaction");
    }

    @Test
    void should_execute_true_when_wallet_transaction_success() throws InvalidTransactionException {
        when(lock.lock("t_test_id2")).thenReturn(true);
        WalletService walletService = mock(WalletServiceImpl.class);

        WalletTransaction transaction = new WalletTransaction("t_test_id2", 1L, 1L);
        when(walletService.moveMoney(anyString(), anyLong(), anyLong(), anyDouble())).thenReturn(anyString());

        boolean result = transaction.execute(walletService);
        assertThat(result).isTrue();
    }

    @Test
    void should_execute_false_when_wallet_transaction_failed() throws InvalidTransactionException {
        when(lock.lock("t_test_id2")).thenReturn(true);
        WalletService walletService = mock(WalletServiceImpl.class);

        WalletTransaction transaction = new WalletTransaction("t_test_id2", 1L, 1L);
        when(walletService.moveMoney(anyString(), anyLong(), anyLong(), anyDouble())).thenReturn(null);

        boolean result = transaction.execute(walletService);
        assertThat(result).isFalse();
    }
}