package com.jianli.sys.workflow.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.jianli.common.Ext;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;
import com.jianli.common.domain.validate.Length;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="workflow_activity")
public class WorkflowActivity extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="流程id")
    @Column(name="workflowId")
    public Long workflowId;

    @Display(name="流程实例id")
    @Column(name="instanceId")
    public Long instanceId;

    @Display(name="流程实例编号")
    @Column(name="instanceCode")
    public String instanceCode;

    @Display(name="前步骤id")
    @Column(name="fromStepId")
    public Long fromStepId;

    @Display(name="步骤id")
    @Column(name="stepId")
    public Long stepId;

    @Display(name="流转id")
    @Column(name="flowId")
    public Long flowId;

    @Display(name="用户id")
    @Column(name="userId")
    public Long userId;

    @Display(name="流程活动上下文")
    @Column(name="activityContext")
    public String activityContext;

    @Display(name="接收时间")
    @Column(name="receiveTime")
    public Date receiveTime;

    @Display(name="处理时间")
    @Column(name="processTime")
    public Date processTime;

    @Display(name="返回时间")
    @Column(name="returnTime")
    public Date returnTime;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Length(length = 512)
    @Display(name="备注")
    @Column(name="remark")
    public String remark;

    private WorkflowActivity.Context context;

    public WorkflowActivity.Context getContext() {
        if(!Ext.isNullOrEmpty(activityContext))
        {
            context = new WorkflowActivity.Context(activityContext);
        }
        return context;
    }

    public void setContext(WorkflowActivity.Context context) {
        this.context = context;
        if(context != null && context.data.size() > 0) {
            activityContext = this.context.toString();
        }
        else
        {
            activityContext = null;
        }
    }

    public static class Status
    {
        //未接收
        public static Integer NotReceived = 1;

        //已接收
        public static Integer Received = 2;

        //已抄送
        public static Integer CopyTo = 3;

        //已处理
        public static Integer Processed = 4;

        //已退回
        public static Integer Returned = 5;

        //已撤回
        public static Integer Recalled = 6;

        public static String getStatusName(Integer status)
        {
            if(status != null) {
                if (status.equals(NotReceived)) {
                    return "未读";
                } else if (status.equals(Received)) {
                    return "已读";
                } else if (status.equals(CopyTo)) {
                    return "抄送";
                } else if (status.equals(Processed)) {
                    return "已处理";
                } else if (status.equals(Returned)) {
                    return "已退回";
                } else if (status.equals(Recalled)) {
                    return "已撤回";
                }
            }
            return "";
        }
    }

    public static class Context
    {
        private JSONObject data = null;

        public Context() {
            data = new JSONObject(true);
        }

        public Context(String json) {
            data = JSONObject.parseObject(json, Feature.OrderedField);
        }

        public String toString(){
            return data().toString();
        }

        public JSONObject data()
        {
            return data;
        }

        public String getToOrgIds()
        {
            return data().getString("toOrgIds");
        }

        public void setToOrgIds(String orgIds)
        {
            data().put("toOrgIds", orgIds);
        }

        public String getToUserIds()
        {
            return data().getString("toUserIds");
        }

        public void setToUserIds(String userIds)
        {
            data().put("toUserIds", userIds);
        }

        public String getCondition()
        {
            return data().getString("condition");
        }

        public void setCondition(String condition)
        {
            data().put("condition", condition);
        }

        public Long getReturnTo()
        {
            return data().getLong("returnTo");
        }

        public void setReturnTo(String returnTo)
        {
            data().put("returnTo", returnTo);
        }

        public void set(JSONObject data)
        {
            setToOrgIds(data.getString("toOrgIds"));
            setToUserIds(data.getString("toUserIds"));
            setCondition(data.getString("condition"));
            setReturnTo(data.getString("returnTo"));
        }
    }
}
