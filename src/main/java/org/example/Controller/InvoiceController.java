package org.example.Controller;

import org.example.DTO.response.InvoiceResponse;
import org.example.Service.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/orders/{orderId}/invoice")
    @PreAuthorize("isAuthenticated()")
    public InvoiceResponse uploadInvoice(
            @PathVariable int orderId,
            @RequestPart("file") MultipartFile file) {
        return invoiceService.upload(orderId, file);
    }

    @GetMapping("/orders/{orderId}/invoice")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadInvoice(@PathVariable int orderId) {
        InvoiceService.StoredInvoice stored = invoiceService.download(orderId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(stored.invoice().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(stored.invoice().getOriginalFilename())
                                .build().toString())
                .body(new FileSystemResource(stored.path()));
    }
}
