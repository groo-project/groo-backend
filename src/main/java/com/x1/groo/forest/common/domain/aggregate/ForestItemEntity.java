package com.x1.groo.forest.common.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name="forest_item")
public class ForestItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="total_count")
    private int totalCount;

    @Column(name="placed_count")
    private int placedCount;

    @Column(name="item_id")
    private int itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="forest_id")
    private ForestEntity forest;

    // placed_count를 -1 하는 로직
    public void decreasePlacedCount() {
        if (this.placedCount <= 0) {
            throw new IllegalStateException("배치된 아이템이 없습니다.");
        }
        this.placedCount--;
    }

    // placed_count를 +1 하는 로직
    public void incrementPlacedCount() {
        this.placedCount++;
    }

    // total_count를 +1 하는 로직
    public void incrementTotalCount() { this.totalCount++; }
}
