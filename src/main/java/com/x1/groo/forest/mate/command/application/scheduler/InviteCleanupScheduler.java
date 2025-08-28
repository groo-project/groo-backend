package com.x1.groo.forest.mate.command.application.scheduler;

import com.x1.groo.forest.mate.command.domain.repository.ForestInviteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteCleanupScheduler {

    private final ForestInviteRepository forestInviteRepository;

    @Transactional
    @Scheduled(cron = "0 5 * * * *", zone = "Asia/Seoul")
    public void purge() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1); // 만료 1일 뒤 삭제
        int purged = forestInviteRepository.deleteExpired(cutoff);
        log.info("purged expired invites (cutoff={}): {}", cutoff, purged);

    }
}
