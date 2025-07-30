package com.x1.groo.forest.mate.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "shared_forest")
public class SharedForestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, name = "user_id")
    private int userId;

    @Column(nullable = false, name = "forest_id")
    private int forestId;

    public SharedForestEntity(int userId, int forestId) {
        this.userId = userId;
        this.forestId = forestId;
    }
}
