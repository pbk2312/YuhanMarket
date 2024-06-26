package hello.yuhanmarket.domain.shopping;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Order order;

    private int orderPrice; //주문 가격

    private int count; //수량

    private LocalDateTime regTime;

    private LocalDateTime updateTime;


}
