package com.tricol.Tricol.repository;

import com.tricol.Tricol.model.StockOutboundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockOutboundItemRepository extends JpaRepository<StockOutboundItem, Long> {
}