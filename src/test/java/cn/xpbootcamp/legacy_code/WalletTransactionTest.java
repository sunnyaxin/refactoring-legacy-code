package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Test;

import javax.transaction.InvalidTransactionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletTransactionTest {

    @Test
    void transaction_execute_false_when_lock_failed() throws InvalidTransactionException {
        WalletTransaction transaction = new WalletTransaction("test_id", 1L, 1L);
        RedisDistributedLock lock = mock(RedisDistributedLock.class);
        new MockUp<RedisDistributedLock>() {
            @Mock
            public RedisDistributedLock getSingletonInstance() {
                return lock;
            }
        };
        when(lock.lock(anyString())).thenReturn(false);

        boolean result = transaction.execute();

        assertThat(result).isFalse();
    }
}