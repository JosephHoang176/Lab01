package org.example.Repository;

import org.example.Entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findFirstByOrderIdOrderByUploadedAtDesc(int orderId);
}
