package org.example.Service;

import org.example.DTO.response.InvoiceResponse;
import org.example.Entity.Invoice;
import org.example.Entity.Order;
import org.example.Repository.InvoiceRepository;
import org.example.Repository.OrderJPARepository;
import org.example.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class InvoiceService {
    public static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "application/pdf", ".pdf",
            "image/png", ".png",
            "image/jpeg", ".jpg"
    );

    private final InvoiceRepository invoiceRepository;
    private final OrderJPARepository orderRepository;
    private final Path storageRoot;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            OrderJPARepository orderRepository,
            @Value("${app.invoice.storage-dir:./data/invoices}") String storageDirectory) {
        this.invoiceRepository = invoiceRepository;
        this.orderRepository = orderRepository;
        this.storageRoot = Paths.get(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public InvoiceResponse upload(int orderId, MultipartFile file) {
        if (orderId <= 0 || !orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order khong ton tai");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order khong ton tai"));
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Invoice can only be uploaded for a PAID order");
        }
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Invoice file is required");
        }

        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("Invoice file exceeds 10 MiB");
        }
        String contentType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("Unsupported invoice MIME type");
        }

        String originalName = safeOriginalName(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + extension;
        Path target = storageRoot.resolve(storedName).normalize();
        if (!target.getParent().equals(storageRoot)) {
            throw new IllegalStateException("Invalid invoice storage path");
        }

        try {
            Files.createDirectories(storageRoot);
            String sha256;
            try (InputStream input = file.getInputStream()) {
                sha256 = sha256AndCopy(input, target);
            }
            Invoice invoice = new Invoice(orderId, originalName, storedName, contentType,
                    file.getSize(), sha256, target.toString(), OffsetDateTime.now());
            Invoice saved = invoiceRepository.save(invoice);
            return new InvoiceResponse(saved.getId() == null ? 0 : saved.getId(),
                    saved.getOrderId(), saved.getOriginalFilename(), saved.getContentType(),
                    saved.getSizeBytes(), saved.getSha256(), saved.getUploadedAt());
        } catch (IOException e) {
            deleteQuietly(target);
            throw new IllegalStateException("Unable to store invoice", e);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public StoredInvoice download(int orderId) {
        Invoice invoice = invoiceRepository.findFirstByOrderIdOrderByUploadedAtDesc(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice khong ton tai"));
        Path file = storageRoot.resolve(invoice.getStoredFilename()).normalize();
        if (!file.getParent().equals(storageRoot)) {
            throw new IllegalStateException("Invalid invoice storage path");
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("Invoice file is missing");
        }
        return new StoredInvoice(invoice, file);
    }

    private String safeOriginalName(String name) {
        if (name == null || name.isBlank()) return "invoice";
        String normalized = name.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        normalized = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        normalized = normalized.replaceAll("[^a-zA-Z0-9._ -]", "_").trim();
        if (normalized.isBlank()) return "invoice";
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private String sha256AndCopy(InputStream input, Path target) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                byte[] buffer = new byte[8192];
                int read;
                long copied = 0;
                while ((read = input.read(buffer)) != -1) {
                    copied += read;
                    if (copied > MAX_SIZE_BYTES) {
                        throw new IllegalArgumentException("Invoice file exceeds 10 MiB");
                    }
                    digest.update(buffer, 0, read);
                    output.write(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
        }
    }

    public record StoredInvoice(Invoice invoice, Path path) {
    }
}
