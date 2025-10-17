package com.x1.groo.diary.command.domain.repository;

import com.x1.groo.diary.command.domain.entity.DiaryEmotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryEmotionRepository extends JpaRepository<DiaryEmotion, Integer> { }