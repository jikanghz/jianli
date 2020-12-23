package com.jianli.sys.workflow.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.jianli.common.Ext;
import com.jianli.common.domain.Display;
import com.jianli.common.domain.EntityDomain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name="workflow_instance")
public class WorkflowInstance extends EntityDomain {

    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name="id")
    @Display(name="id")
    public Long id;

    @Display(name="流程实例编号")
    @Column(name="instanceCode")
    public String instanceCode;

    @Display(name="流程实例上下文")
    @Column(name="instanceContext")
    public String instanceContext;

    @Display(name="流程id")
    @Column(name="workflowId")
    public Long workflowId;

    @Display(name="业务对象id")
    @Column(name="entityId")
    public Long entityId;

    @Display(name="流转id")
    @Column(name="flowId")
    public Long flowId;

    @Display(name="单位id")
    @Column(name="tenantId")
    public Long tenantId;

    @Display(name="结束人")
    @Column(name="finishBy")
    public Long finishBy;

    @Display(name="结束时间")
    @Column(name="finishTime")
    public Date finishTime;


    private Context context;

    public Context getContext() {
        if(!Ext.isNullOrEmpty(instanceContext))
        {
            context = new Context(instanceContext);
        }
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        if(context != null && context.data.size() > 0) {
            instanceContext = this.context.toString();
        }
        else
        {
            instanceContext = null;
        }
    }

    public static class Status
    {
        //运行中
        public static Integer Running = 1;

        //已完成
        public static Integer Completed = 2;

        //已终止
        public static Integer Terminated = 3;

        public static String getStatusName(Integer status)
        {
            if(status!= null) {
                if (status.equals(Running)) {
                    return "运行中";
                } else if (status.equals(Completed)) {
                    return "已完成";
                } else if (status.equals(Terminated)) {
                    return "已终止";
                }
            }
            return null;
        }
    }


    public static class Context
    {
        private JSONObject data = null;

        public Context()
        {
            data = new JSONObject(true);
        }

        public Context(String json)
        {
            data = JSONObject.parseObject(json, Feature.OrderedField);
        }

        public String toString(){
            return data().toString();
        }

        public JSONObject data()
        {
            return data;
        }

        public String getOrgIds()
        {
            return data().getString("orgIds");
        }

        public void setOrgIds(String orgIds)
        {
            data().put("orgIds", orgIds);
        }

    }
}
