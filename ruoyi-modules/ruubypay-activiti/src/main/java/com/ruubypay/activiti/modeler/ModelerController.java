package com.ruubypay.activiti.modeler;

import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_DESCRIPTION;
import static org.activiti.editor.constants.ModelDataJsonConstants.MODEL_NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.pagehelper.Page;
import com.ruoyi.common.core.web.controller.BaseController;
import com.ruoyi.common.core.web.domain.AjaxResult;
import com.ruoyi.common.core.web.page.PageDomain;
import com.ruoyi.common.core.web.page.TableDataInfo;
import com.ruoyi.common.core.web.page.TableSupport;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ModelEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * ?????????????????????
 *
 * @author ????????????
 */
@Controller
@RequestMapping("/modeler")
@AllArgsConstructor
public class ModelerController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelEditorJsonRestResource.class);

    private RepositoryService repositoryService;

    /**
     * ????????????
     */
    @PreAuthorize("@ss.hasPermi('activiti:modeler:list')")
    @GetMapping("/list")
    @ResponseBody
    public TableDataInfo list(ModelEntityImpl modelEntity) {
        ModelQuery modelQuery = repositoryService.createModelQuery();
        modelQuery.orderByLastUpdateTime().desc();

        // ????????????
        if (StringUtils.isNotBlank(modelEntity.getKey())) {
            modelQuery.modelKey(modelEntity.getKey());
        }
        if (StringUtils.isNotBlank(modelEntity.getName())) {
            modelQuery.modelNameLike("%" + modelEntity.getName() + "%");
        }

        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        List<Model> resultList = modelQuery.listPage((pageNum - 1) * pageSize, pageSize);
        Page<ModelEntityImpl> list = new Page<>();
        resultList.parallelStream().forEach(model -> {
            ModelEntityImpl modelEntity1 = (ModelEntityImpl) model;
            list.add(modelEntity1);
        });

        list.setTotal(modelQuery.count());
        list.setPageNum(pageNum);
        list.setPageSize(pageSize);

        return getDataTable(list);
    }

    /**
     * ????????????
     */
    @PostMapping(value = "/create")
    @ResponseBody
    public AjaxResult create(@RequestParam("name") String name, @RequestParam("key") String key,
                             @RequestParam(value = "description", required = false) String description) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            description = StringUtils.defaultString(description);
            modelObjectNode.put(MODEL_DESCRIPTION, description);

            Model newModel = repositoryService.newModel();
            newModel.setMetaInfo(modelObjectNode.toString());
            newModel.setName(name);
            newModel.setKey(StringUtils.defaultString(key));

            repositoryService.saveModel(newModel);
            repositoryService.addModelEditorSource(newModel.getId(), editorNode.toString().getBytes(StandardCharsets.UTF_8));
            logger.info("?????????????????????[{}]", newModel.getName());
            return new AjaxResult(200, "??????????????????", newModel.getId());
        } catch (Exception e) {
            logger.error("?????????????????????", e);
        }
        return error();
    }

    /**
     * ??????Model????????????
     */
    @GetMapping(value = "/deploy/{modelId}")
    @ResponseBody
    public AjaxResult deploy(@PathVariable("modelId") String modelId) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName()).addString(processName, new String(bpmnBytes, "UTF-8")).deploy();
            LOGGER.info("?????????????????????ID=" + deployment.getId());
            return success("????????????");
        } catch (Exception e) {
            LOGGER.error("?????????????????????????????????modelId={}", modelId, e);

        }
        return error("????????????");
    }

    /**
     * ??????model???xml??????
     */
    @GetMapping(value = "/export/{modelId}")
    public void export(@PathVariable("modelId") String modelId, HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);

            // ??????????????????
            if (!CollectionUtils.isEmpty(bpmnModel.getProcesses())) {
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

                ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
                String filename = bpmnModel.getMainProcess().getId() + ".bpmn";
                response.setHeader("Content-Disposition", "attachment; filename=" + filename);
                IOUtils.copy(in, response.getOutputStream());
                response.flushBuffer();
            }

        } catch (Exception e) {
            LOGGER.error("??????model???xml???????????????modelId={}", modelId, e);
        }
    }

    @DeleteMapping("/remove/{ids}")
    @ResponseBody
    public AjaxResult remove(@PathVariable("ids") String ids) {
        try {
            repositoryService.deleteModel(ids);
            return AjaxResult.success();
        }
        catch (Exception e) {
            return error(e.getMessage());
        }
    }

}
