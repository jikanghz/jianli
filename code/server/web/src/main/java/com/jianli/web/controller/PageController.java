package com.jianli.web.controller;

import com.jianli.common.controller.ControllerUtil;
import com.jianli.common.oss.Oss;
import com.jianli.common.util.SecurityUtil;
import com.jianli.sys.service.SecurityService;
import com.jianli.sys.service.SysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class PageController {
    @Resource
    private SecurityUtil securityUtil;
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private Oss oss;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private ControllerUtil controllerUtil;

    @GetMapping("/")
    public String index(){ return "login"; }

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response) {
        return "login";
    }

    @GetMapping("/main")
    public String main(HttpServletRequest request, HttpServletResponse response) {
        return "main";
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request, HttpServletResponse response) {
        return "home";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        securityService.logout(controllerUtil.getToken(request));
        response.setStatus(302);
        response.setHeader("location", "login");
        return "login";
    }

    @GetMapping("/tenant")
    public String tenant(HttpServletRequest request, HttpServletResponse response) {
        return "tenant";
    }

    @GetMapping("/org")
    public String org(HttpServletRequest request, HttpServletResponse response) {
        return "org";
    }

    @GetMapping("/role")
    public String role(HttpServletRequest request, HttpServletResponse response) {
        return "role";
    }

    @GetMapping("/module")
    public String module(HttpServletRequest request, HttpServletResponse response) {
        return "module";
    }

    @GetMapping("/user")
    public String user(HttpServletRequest request, HttpServletResponse response) {
        return "user";
    }

    @GetMapping("/notice")
    public String notice(HttpServletRequest request, HttpServletResponse response) {
        return "notice";
    }

    @GetMapping("/cash")
    public String cash(HttpServletRequest request, HttpServletResponse response) {
        return "cash";
    }

    @GetMapping("/project")
    public String project(HttpServletRequest request, HttpServletResponse response) {
        return "project";
    }


}
