package com.x1.groo.diary.repository;

import com.x1.groo.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    List<Diary> findAllByUserIdAndIsPublishedFalse(int userId);
}