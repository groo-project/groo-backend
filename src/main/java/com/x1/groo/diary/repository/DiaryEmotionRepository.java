package com.x1.groo.diary.repository;

import com.x1.groo.diary.entity.DiaryEmotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryEmotionRepository extends JpaRepository<DiaryEmotion, Integer> { }