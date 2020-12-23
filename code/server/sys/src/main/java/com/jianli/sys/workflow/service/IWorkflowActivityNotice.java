package com.jianli.sys.workflow.service;

import com.jianli.sys.workflow.domain.WorkflowActivity;
import com.jianli.sys.workflow.domain.WorkflowInstance;

public interface IWorkflowActivityNotice {
    public void onProcessed(WorkflowInstance instance, WorkflowActivity from, WorkflowActivity to, Long userId);

    public void onReturned(WorkflowInstance instance, WorkflowActivity from, WorkflowActivity to, Long userId);
}
