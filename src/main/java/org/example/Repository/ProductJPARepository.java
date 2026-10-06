package org.example.Repository;

import org.example.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

@Repository
public interface ProductJPARepository extends JpaRepository<Product, Integer> {

    @Modifying
    @Query("""
            update Product p
               set p.stock = p.stock - :quantity
             where p.id = :productId
               and p.stock >= :quantity
            """)
    int reserveStock(
            @Param("productId") int productId,
            @Param("quantity") int quantity
    );
}
