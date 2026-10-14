package org.example.config;

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class CorePhysicalNamingStrategy extends CamelCaseToUnderscoresNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        if (logicalName != null && "PaymentWebhookEvents".equals(logicalName.getText())) {
            return Identifier.toIdentifier("PaymentWebhookEvents", true);
        }
        return super.toPhysicalTableName(logicalName, jdbcEnvironment);
    }
}
