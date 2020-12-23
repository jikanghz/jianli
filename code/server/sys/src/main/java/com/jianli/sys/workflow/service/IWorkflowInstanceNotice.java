package com.jianli.sys.workflow.service;

import com.jianli.sys.workflow.domain.WorkflowInstance;

public interface IWorkflowInstanceNotice {
    public void onStarted(WorkflowInstance instance, Long userId);

    public void onCompleted(WorkflowInstance instance, Long userId);

    public void onStoped(WorkflowInstance instance, Long userId);
}
