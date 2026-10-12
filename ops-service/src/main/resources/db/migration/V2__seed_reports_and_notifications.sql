IF NOT EXISTS (SELECT 1 FROM dbo.reports WHERE report_date = '2026-10-09' AND currency = 'VND')
    INSERT INTO dbo.reports (
        report_date, currency, orders_created_count, orders_paid_count,
        orders_fulfilled_count, orders_cancelled_count, pending_payment_count_eod,
        orders_created_amount, payments_received_amount
    ) VALUES ('2026-10-09', 'VND', 10, 8, 6, 1, 2, 1500000.00, 1200000.00);

IF NOT EXISTS (SELECT 1 FROM dbo.notifications WHERE source_service = 'ops-seed' AND source_event_id = 900001)
    INSERT INTO dbo.notifications (
        source_event_id, source_service, event_type, order_id, order_code,
        previous_status, new_status, occurred_at
    ) VALUES (900001, 'ops-seed', 'order.created', 700001, 'SEED-700001', NULL, 'DRAFT', SYSDATETIMEOFFSET());
