package com.tricol.inventory_management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_order_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private SupplierOrder supplierOrder;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    public void calculateTotalAmount() {
        if (unitPrice != null && quantity > 0) {
            this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        } else {
            this.totalAmount = BigDecimal.ZERO;
        }
    }
}
