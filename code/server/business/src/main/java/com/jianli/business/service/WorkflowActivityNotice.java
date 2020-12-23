package com.jianli.business.service;

import com.jianli.sys.workflow.domain.WorkflowActivity;
import com.jianli.sys.workflow.domain.WorkflowInstance;
import com.jianli.sys.workflow.service.IWorkflowActivityNotice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("workflowActivityNotice")
public class WorkflowActivityNotice implements IWorkflowActivityNotice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void onProcessed(WorkflowInstance instance, WorkflowActivity from, WorkflowActivity to, Long userId)
    {
        logger.info("onProcessed:" + instance.instanceCode + ":" + "from:" + from.stepId + "to:" + to.stepId);
    }

    public void onReturned(WorkflowInstance instance, WorkflowActivity from, WorkflowActivity to, Long userId)
    {
        logger.info("onReturned:" + instance.instanceCode + ":" + "from:" + from.stepId + "to:" + to.stepId);
    }
}
