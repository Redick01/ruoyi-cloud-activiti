package com.ruubypay.activiti.configure;

import java.io.IOException;
import javax.sql.DataSource;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Redick01
 */
@ComponentScan(basePackages = {"org.activiti.rest"}, includeFilters = {
        @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = {
                RestController.class})}, useDefaultFilters = false, lazyInit = false)
@Configuration
public class ActivitiConfig implements ProcessEngineConfigurationConfigurer {

    @Autowired
    private ICustomProcessDiagramGenerator customProcessDiagramGenerator;

    /**
     * 解決工作流生成图片乱码问题
     *
     * @param processEngineConfiguration
     */
    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setProcessDiagramGenerator(customProcessDiagramGenerator);
    }

    @Bean
    public ProcessEngine processEngine(DataSourceTransactionManager transactionManager, DataSource dataSource) throws IOException {
        SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
        //自动部署已有的流程文件
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(
                ResourceLoader.CLASSPATH_URL_PREFIX + "processes/*.bpmn");
        configuration.setTransactionManager(transactionManager);
        // 执行工作流对应的数据源
        configuration.setDataSource(dataSource);
        // 是否自动创建流程引擎表
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
        configuration.setDeploymentResources(resources);
        //configuration.setDbIdentityUsed(false);
        //configuration.setAsyncExecutorActivate(false);
        // 流程历史等级
        //configuration.setHistoryLevel(HistoryLevel.FULL);
        // 流程图字体
        configuration.setActivityFontName("宋体");
        configuration.setAnnotationFontName("宋体");
        configuration.setLabelFontName("宋体");
        return configuration.buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }

    @Bean
    public IdentityService identityService(ProcessEngine processEngine) {
        return processEngine.getIdentityService();
    }

    @Bean
    public FormService formService(ProcessEngine processEngine) {
        return processEngine.getFormService();
    }
}
