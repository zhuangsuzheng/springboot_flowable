package com.yykjc.module.controller;

import org.apache.log4j.Logger;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/flow")
@RestController
public class FlowController {

    @Autowired
    RepositoryService repositoryService;

    private static Logger log = Logger.getLogger(FlowController.class);

    @GetMapping("/start/{filePath}")
    public Map<String, Object> createFlow(@RequestParam("filePath") String filePath) {

        System.out.println("filePath = " + filePath);
        Map<String, Object> res = new HashMap<>();
        //解析BPMN模型看是否成功
        XMLStreamReader reader = null;
        InputStream inputStream = null;
        try {
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            XMLInputFactory factory = XMLInputFactory.newInstance();
            inputStream = new FileInputStream(new File(filePath));
            reader = factory.createXMLStreamReader(inputStream);
            BpmnModel model = bpmnXMLConverter.convertToBpmnModel(reader);
            List<Process> processes = model.getProcesses();
            Process curProcess = null;
            if (CollectionUtils.isEmpty(processes)) {
                log.error("BPMN模型没有配置流程1");
                return null;
            }
            res.put("processes", processes);
            curProcess = processes.get(0);

            inputStream = new FileInputStream(new File(filePath));
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().name("TEST_FLOW")
                    .addInputStream(filePath, inputStream);

            Deployment deployment = deploymentBuilder.deploy();
            res.put("deployment", deployment);
            log.warn("部署流程 name:" + curProcess.getName() + " key " + deployment.getKey() + " deploy " + deployment);
            return res;
        } catch (Exception e) {
            log.error("BPMN模型创建流程异常", e);
            return null;
        } finally {
            try {
                reader.close();
            } catch (XMLStreamException e) {
                log.error("关闭异常", e);
            }
        }
    }

}
