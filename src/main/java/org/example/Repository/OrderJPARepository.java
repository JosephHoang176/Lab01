package org.example.Repository;

import org.example.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderJPARepository extends JpaRepository<Order, Integer> {
}
