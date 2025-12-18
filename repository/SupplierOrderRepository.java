package com.tricol.Tricol.repository;

import com.tricol.Tricol.enums.OrderStatus;
import com.tricol.Tricol.model.SupplierOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SupplierOrderRepository extends JpaRepository<SupplierOrder , Long> {
    List<SupplierOrder> findBySupplierId(Long supplierId);
    List<SupplierOrder> findByStatus(OrderStatus status);
    List<SupplierOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
}
