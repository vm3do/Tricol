package com.tricol.Tricol.specification;

import com.tricol.Tricol.enums.MovementType;
import com.tricol.Tricol.model.StockMovement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class StockMovementSpecifications {

    public static Specification<StockMovement> hasProductId(Long productId) {
        return (root, query, criteriaBuilder) -> {
            if (productId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<StockMovement> hasProductReference(String reference) {
        return (root, query, criteriaBuilder) -> {
            if (reference == null || reference.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("product").get("reference"), reference);
        };
    }

    public static Specification<StockMovement> hasMovementType(MovementType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("movementType"), type);
        };
    }

    public static Specification<StockMovement> hasLotNumber(String lotNumber) {
        return (root, query, criteriaBuilder) -> {
            if (lotNumber == null || lotNumber.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("stockLot").get("lotNumber"), lotNumber);
        };
    }

    public static Specification<StockMovement> movementDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return criteriaBuilder.conjunction();
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("movementDate"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("movementDate"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("movementDate"), endDate);
        };
    }
}