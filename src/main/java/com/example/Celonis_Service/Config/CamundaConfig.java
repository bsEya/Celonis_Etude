package com.example.Celonis_Service.Config;

import com.example.Celonis_Service.Service.CamundaElasticHistoryHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.springframework.context.annotation.Configuration;
import org.camunda.bpm.engine.ProcessEngine;

import org.springframework.context.annotation.Lazy;

@Configuration
public class CamundaConfig implements ProcessEnginePlugin {

    private final CamundaElasticHistoryHandler historyHandler;

    public CamundaConfig(@Lazy CamundaElasticHistoryHandler historyHandler) {
        this.historyHandler = historyHandler;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl config) {
        config.setHistoryLevel(HistoryLevel.HISTORY_LEVEL_FULL);
        config.setHistoryEventHandler(historyHandler);
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl config) {}

    @Override
    public void postProcessEngineBuild(ProcessEngine processEngine) {}
}