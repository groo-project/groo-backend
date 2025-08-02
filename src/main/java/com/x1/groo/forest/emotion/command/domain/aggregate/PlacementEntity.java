package com.x1.groo.forest.emotion.command.domain.aggregate;

import com.x1.groo.forest.common.domain.aggregate.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name="placement")
public class PlacementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name = "position_x", precision = 9, scale = 6)
    private BigDecimal positionX;

    @Column(name = "position_y", precision = 9, scale = 6)
    private BigDecimal positionY;

    @Column(name = "width", precision = 6, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 6, scale = 2)
    private BigDecimal height;

    @Column(name = "z_index")
    private Integer zIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_item_id")
    private UserItemEntity userItem;

    public PlacementEntity(BigDecimal positionX, BigDecimal positionY,
                           UserEntity user, UserItemEntity userItem) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.user = user;
        this.userItem = userItem;
    }
}
