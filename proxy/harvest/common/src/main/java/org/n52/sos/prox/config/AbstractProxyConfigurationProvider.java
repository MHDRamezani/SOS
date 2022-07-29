package org.n52.sos.prox.config;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.n52.bjornoya.schedule.DefaultJobConfiguration;
import org.n52.bjornoya.schedule.JobConfiguration;
import org.n52.bjornoya.schedule.JobConfiguration.JobType;
import org.n52.iceland.service.DatasourceSettingsHandler;
import org.n52.sensorweb.server.helgoland.adapters.config.ConfigurationProvider;
import org.n52.sensorweb.server.helgoland.adapters.config.DataSourceConfiguration;
import org.n52.sos.ds.datasource.ProxyDatasource;

public abstract class AbstractProxyConfigurationProvider implements ConfigurationProvider {

    @Inject
    private DatasourceSettingsHandler datasourceSettingsHandler;

    @Inject
    private DefaultJobConfiguration defaultJobConfiguration;

    @Override
    public List<DataSourceConfiguration> getDataSources() {
        return Collections.singletonList(getDataSourceConfiguration());
    }

    public DataSourceConfiguration getDataSourceConfiguration() {
        DataSourceConfiguration config = new DataSourceConfiguration();
        config.setItemName(getItemName());
        config.setConnector(getConnector());
        config.setUrl(getUrl());
        config.setType(getType());
        addValues(config);
        config.addJobs(getFullHarvestJob());
        config.addJobs(getTemporalHarvestJob());
        return config;
    }

    protected abstract String getGroup();

    protected abstract String getName();

    protected abstract String getType();

    protected abstract String getConnector();

    protected abstract String getItemName();

    protected String getRestPath() {
        return getProperties().getProperty(ProxyDatasource.PROXY_PATH_KEY, "");
    }

    private String getUrl() {
        return getProperties().getProperty(ProxyDatasource.PROXY_HOST_KEY) + getRestPath();
    }

    private DataSourceConfiguration addValues(DataSourceConfiguration config) {
        if (getProperties().containsKey(ProxyDatasource.PROXY_USERNAME_KEY)) {
            config.setUsername(getProperties().getProperty(ProxyDatasource.PROXY_USERNAME_KEY));
        }
        if (getProperties().containsKey(ProxyDatasource.PROXY_PASSWORD_KEY)) {
           config.setPassword(getProperties().getProperty(ProxyDatasource.PROXY_PASSWORD_KEY));
        }
        if (isProxyDefined()) {
            config.addProperty(ProxyDatasource.PROXY_PROXY_HOST_KEY,
                    getProperties().getProperty(ProxyDatasource.PROXY_PROXY_HOST_KEY));
            String portValue = getProperties().getProperty(ProxyDatasource.PROXY_PROXY_PORT_KEY);
            if (portValue != null && !portValue.isEmpty()) {
                config.addProperty(ProxyDatasource.PROXY_PROXY_PORT_KEY, portValue);
            }
        }
        return config;
    }

    private JobConfiguration addValues(JobConfiguration config) {
        return config.setGroup(getGroup());
    }

    private boolean isProxyDefined() {
        return getProperties().containsKey(ProxyDatasource.PROXY_PROXY_HOST_KEY);
    }

    private Properties getProperties() {
        return this.datasourceSettingsHandler.getAll();
    }

    private JobConfiguration getFullHarvestJob() {
        return addValues(defaultJobConfiguration.getFullJobConfiguration(getJobName(JobType.full)));
    }

    private String getJobName(JobType jobType) {
        return getName() + " - " + jobType.name();
    }

    private JobConfiguration getTemporalHarvestJob() {
        return addValues(defaultJobConfiguration.getTemporalJobConfiguration(getJobName(JobType.temporal)));
    }
}