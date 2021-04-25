package org.procj.provider.hibernate;

import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.procj.provider.spi.ProcedureExecutor;
import org.procj.provider.spi.ProcedureExecutorProvider;

public class HibernateProcedureExecutorProvider implements ProcedureExecutorProvider {

	@Override
	public ProcedureExecutor initExecutor(Properties properties) {
		final StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
		if (properties.containsKey("hibernate-config-file")) {
			registryBuilder.configure(properties.getProperty("hibernate-config-file"));
		}
		for (final Object p : properties.keySet()) {
			registryBuilder.applySetting(p.toString(), properties.getProperty(p.toString()));
		}
		final Metadata meta = new MetadataSources(registryBuilder.build()).getMetadataBuilder().build();
		final SessionFactory sf = meta.buildSessionFactory();
		return new HibernateProcedureExecutor(sf.openSession());
	}

	@Override
	public String getName() {
		return "hibernate";
	}
}
