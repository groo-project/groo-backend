package com.x1.groo.diary.command.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_selection_draft")
@Getter
@Setter
@NoArgsConstructor
public class ItemSelectionDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    // 일기와의 1:1 관계 (논리적 Unique)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false, unique = true)
    private Diary diary;

    @Column(name = "item_id_1", nullable = false)
    private Integer itemId1;

    @Column(name = "item_id_2", nullable = false)
    private Integer itemId2;

    @Column(name = "item_id_3", nullable = false)
    private Integer itemId3;
}
