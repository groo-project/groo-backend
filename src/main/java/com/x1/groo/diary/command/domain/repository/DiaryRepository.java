package com.x1.groo.diary.command.domain.repository;

import com.x1.groo.diary.command.domain.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    void deleteAllByUserId(int userId);
}