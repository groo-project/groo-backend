package com.x1.groo.forest.emotion.command.domain.repository;

import com.x1.groo.forest.emotion.command.domain.aggregate.MailboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MailboxRepository extends JpaRepository<MailboxEntity, Integer> {

    @Modifying
    @Query("UPDATE MailboxEntity m SET m.isDeleted = true WHERE m.id = :mailboxId")
    void softDeleteById(@Param("mailboxId") int mailboxId);
}
