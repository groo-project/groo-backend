package com.x1.groo.common.scheduler;

import com.x1.groo.email.repository.EmailRepository;
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
    private final EmailRepository emailRepository;

    @Transactional
    @Scheduled(cron = "0 */15 * * * *", zone = "Asia/Seoul")
    public void purge() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1);

        // 초대코드 삭제
        int forestInvitePurged = forestInviteRepository.deleteExpired(cutoff);

        // 이메일 인증코드 삭제
        int emailPurged = emailRepository.deleteExpired(cutoff);

        log.info("purged expired invites (cutoff={}): {}", cutoff, forestInvitePurged);
        log.info("purged expired email (cutoff={}): {}", cutoff, emailPurged);

    }
}
