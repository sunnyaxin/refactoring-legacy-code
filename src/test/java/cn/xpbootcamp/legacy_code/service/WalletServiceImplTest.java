package cn.xpbootcamp.legacy_code.service;

import cn.xpbootcamp.legacy_code.entity.User;
import cn.xpbootcamp.legacy_code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WalletServiceImplTest {

    private UserRepository repository;
    private WalletService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        service = new WalletServiceImpl(repository);
    }

    @Test
    void should_return_wallet_transaction_id_when_user_balance_more_than_amount() {
        User mockUser = new User(1L, 2);
        when(repository.find(anyLong())).thenReturn(mockUser);

        String result = service.moveMoney("test_id", 1L, 1L, 1);

        assertThat(result).contains("test_id");
    }

    @Test
    void should_return_null_when_user_balance_less_than_amount() {
        User mockUser = new User(1L, 2);
        when(repository.find(anyLong())).thenReturn(mockUser);

        String result = service.moveMoney("test_id", 1L, 1L, 4);

        assertThat(result).isNull();
    }
}