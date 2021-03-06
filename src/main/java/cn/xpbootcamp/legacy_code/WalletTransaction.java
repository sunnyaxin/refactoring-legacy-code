package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletService;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;

public class WalletTransaction {
    private String id;
    private Long buyerId;
    private Long sellerId;
    private Long createdTimestamp;
    private double amount;
    private STATUS status;

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId) {
        id = generateIdBy(preAssignedId);
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public boolean execute(WalletService walletService) throws InvalidTransactionException {
        validateBasicInfo();

        if (status == STATUS.EXECUTED) return true;
        boolean isLocked = false;
        try {
            isLocked = RedisDistributedLock.getSingletonInstance().lock(id);
            if (!isLocked) {
                return false;
            }
            if (status == STATUS.EXECUTED) return true;

            if (isExpired()) return false;

            String walletTransactionId = walletService.moveMoney(id, buyerId, sellerId, amount);

            return finishTransaction(walletTransactionId);
        } finally {
            if (isLocked) {
                RedisDistributedLock.getSingletonInstance().unlock(id);
            }
        }
    }

    private String generateIdBy(String preAssignedId) {
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.id = preAssignedId;
        } else {
            this.id = IdGenerator.generateTransactionId();
        }
        if (!this.id.startsWith("t_")) {
            this.id = "t_" + preAssignedId;
        }
        return id;
    }

    private void validateBasicInfo() throws InvalidTransactionException {
        if (buyerId == null || sellerId == null || amount < 0.0) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
    }

    private boolean isExpired() {
        long executionInvokedTimestamp = System.currentTimeMillis();
        if (executionInvokedTimestamp - createdTimestamp > 1728000000) {
            this.status = STATUS.EXPIRED;
            return true;
        }
        return false;
    }

    private boolean finishTransaction(String walletTransactionId) {
        if (walletTransactionId != null) {
            this.status = STATUS.EXECUTED;
            return true;
        } else {
            this.status = STATUS.FAILED;
            return false;
        }
    }
}