package com.x1.groo.item.domain.storage.aggregate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
@Table(name="user_item")
public class UserItemStorageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name="item_id")
    private int itemId;

    @Column(name="user_id")
    private int userId;

    @Column(name="total_count")
    private int totalCount;

    @Column(name="placed_count")
    private int placedCount;

    @Column(name= "forest_id")
    private int forestId;
}
