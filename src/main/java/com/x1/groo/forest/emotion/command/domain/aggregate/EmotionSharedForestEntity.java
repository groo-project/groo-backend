package com.x1.groo.forest.emotion.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "shared_forest")
public class EmotionSharedForestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    // user_id 컬럼
    @Column(name = "user_id", nullable = false)
    private int userId;

    // forest_id 컬럼
    @Column(name = "forest_id", nullable = false)
    private int forestId;
}