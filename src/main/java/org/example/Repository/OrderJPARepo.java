package org.example.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.Entity.Order;
import org.example.interfaces.IOrderRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class OrderJPARepo implements IOrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return entityManager.createQuery(
                        "select distinct o " +
                                "from org.example.Entity.Order o " +
                                "left join fetch o.lines " +
                                "order by o.createdAt desc",
                        Order.class)
                .getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(int orderId) {
        if (orderId <= 0) {
            return null;
        }
        return entityManager.find(Order.class, orderId);
    }

    @Override
    @Transactional
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order khong duoc null");
        }

        if (entityManager.find(Order.class, order.getId()) == null) {
            entityManager.persist(order);
            return order;
        }

        return entityManager.merge(order);
    }

    @Override
    @Transactional
    public Order update(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order khong duoc null");
        }
        if (order.getId() <= 0
                || entityManager.find(Order.class, order.getId()) == null) {
            throw new IllegalArgumentException(
                    "Khong tim thay order voi id " + order.getId()
            );
        }
        return entityManager.merge(order);
    }
}
