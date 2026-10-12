IF OBJECT_ID(N'dbo.reports', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.reports (
        report_id BIGINT IDENTITY(1,1) NOT NULL,
        report_date DATE NOT NULL,
        currency CHAR(3) NOT NULL CONSTRAINT DF_reports_currency DEFAULT ('VND'),
        orders_created_count INT NOT NULL CONSTRAINT DF_reports_orders_created_count DEFAULT (0),
        orders_paid_count INT NOT NULL CONSTRAINT DF_reports_orders_paid_count DEFAULT (0),
        orders_fulfilled_count INT NOT NULL CONSTRAINT DF_reports_orders_fulfilled_count DEFAULT (0),
        orders_cancelled_count INT NOT NULL CONSTRAINT DF_reports_orders_cancelled_count DEFAULT (0),
        pending_payment_count_eod INT NOT NULL CONSTRAINT DF_reports_pending_payment_count_eod DEFAULT (0),
        orders_created_amount DECIMAL(19,2) NOT NULL CONSTRAINT DF_reports_orders_created_amount DEFAULT (0),
        payments_received_amount DECIMAL(19,2) NOT NULL CONSTRAINT DF_reports_payments_received_amount DEFAULT (0),
        generated_at DATETIMEOFFSET(0) NOT NULL CONSTRAINT DF_reports_generated_at DEFAULT (SYSDATETIMEOFFSET()),
        CONSTRAINT PK_reports PRIMARY KEY (report_id),
        CONSTRAINT UQ_reports_report_date_currency UNIQUE (report_date, currency),
        CONSTRAINT CK_reports_counts_nonnegative CHECK (
            orders_created_count >= 0 AND orders_paid_count >= 0
            AND orders_fulfilled_count >= 0 AND orders_cancelled_count >= 0
            AND pending_payment_count_eod >= 0
        ),
        CONSTRAINT CK_reports_amounts_nonnegative CHECK (
            orders_created_amount >= 0 AND payments_received_amount >= 0
        )
    );
END;

IF OBJECT_ID(N'dbo.notifications', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.notifications (
        notification_id BIGINT IDENTITY(1,1) NOT NULL,
        source_event_id BIGINT NULL,
        source_service VARCHAR(50) NOT NULL CONSTRAINT DF_notifications_source_service DEFAULT ('core-api'),
        event_type VARCHAR(60) NOT NULL,
        order_id BIGINT NOT NULL,
        order_code VARCHAR(30) NOT NULL,
        previous_status VARCHAR(30) NULL,
        new_status VARCHAR(30) NOT NULL,
        occurred_at DATETIMEOFFSET(0) NOT NULL,
        recorded_at DATETIMEOFFSET(0) NOT NULL CONSTRAINT DF_notifications_recorded_at DEFAULT (SYSDATETIMEOFFSET()),
        CONSTRAINT PK_notifications PRIMARY KEY (notification_id),
        CONSTRAINT CK_notifications_event_type CHECK (event_type IN ('order.created', 'order.status_changed')),
        CONSTRAINT CK_notifications_status_values CHECK (
            new_status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'FULFILLED', 'CANCELLED')
            AND (previous_status IS NULL OR previous_status IN ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'FULFILLED', 'CANCELLED'))
        ),
        CONSTRAINT CK_notifications_status_transition CHECK (
            (event_type = 'order.created' AND previous_status IS NULL)
            OR (event_type = 'order.status_changed' AND previous_status IS NOT NULL AND previous_status <> new_status)
        )
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'UX_notifications_source_event' AND object_id = OBJECT_ID(N'dbo.notifications'))
    CREATE UNIQUE INDEX UX_notifications_source_event ON dbo.notifications (source_service, source_event_id) WHERE source_event_id IS NOT NULL;
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_notifications_order_timeline' AND object_id = OBJECT_ID(N'dbo.notifications'))
    CREATE INDEX IX_notifications_order_timeline ON dbo.notifications (order_id, occurred_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_notifications_occurred_at' AND object_id = OBJECT_ID(N'dbo.notifications'))
    CREATE INDEX IX_notifications_occurred_at ON dbo.notifications (occurred_at DESC);
