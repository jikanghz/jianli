package com.jianli.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *   用户页面控制器
 */

@Controller
@RequestMapping(value = "/workflow")
public class WorkflowController extends PageController {

    @GetMapping("/blank")
    public String blank(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/blank";
    }

    @GetMapping("/activityList")
    public String activityList(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activityList";
    }

    @GetMapping("/instanceList")
    public String instanceList(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/instanceList";
    }

    @GetMapping("/monitorList")
    public String monitorList(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/monitorList";
    }

    @GetMapping("/workflowList")
    public String workflowList(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/workflowList";
    }

    @GetMapping("/workflowDesign")
    public String workflowDesign(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/workflowDesign";
    }

    @GetMapping("/activity/project")
    public String project(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/project";
    }
    @GetMapping("/activity/projectApply")
    public String projectApply(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/projectApply";
    }
    @GetMapping("/activity/projectApprove")
    public String projectApprove(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/projectApprove";
    }


    @GetMapping("/activity/cash")
    public String cash(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/cash";
    }
    @GetMapping("/activity/cashApply")
    public String cashApply(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/cashApply";
    }
    @GetMapping("/activity/cashApprove")
    public String cashApprove(HttpServletRequest request, HttpServletResponse response) {
        return "workflow/activity/cashApprove";
    }
}
