package com.clubhub.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class StaffDataUpdateNotifier {

    private final StaffWebSocketHandler staffWebSocketHandler;

    public void notifyAfterCommit(Long memberId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    staffWebSocketHandler.sendMemberDataUpdate(memberId);
                }
            });
            return;
        }

        staffWebSocketHandler.sendMemberDataUpdate(memberId);
    }
}
