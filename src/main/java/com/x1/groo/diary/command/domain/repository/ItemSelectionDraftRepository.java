package com.x1.groo.diary.command.domain.repository;

import com.x1.groo.diary.command.domain.entity.ItemSelectionDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemSelectionDraftRepository extends JpaRepository<ItemSelectionDraft, Integer> {

    Optional<ItemSelectionDraft> findByDiaryId(int diaryId);
}
