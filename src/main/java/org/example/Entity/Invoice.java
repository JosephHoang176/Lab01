package org.example.Entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "Invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "storage_path", nullable = false, length = 1000)
    private String storagePath;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    protected Invoice() {
    }

    public Invoice(int orderId, String originalFilename, String storedFilename,
                   String contentType, long sizeBytes, String sha256,
                   String storagePath, OffsetDateTime uploadedAt) {
        this.orderId = orderId;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256;
        this.storagePath = storagePath;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public int getOrderId() { return orderId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getSha256() { return sha256; }
    public String getStoragePath() { return storagePath; }
    public OffsetDateTime getUploadedAt() { return uploadedAt; }
}
