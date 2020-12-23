# jianli-简单美丽的开发框架和流程引擎

## 1 技术栈 
	SpringBoot、tkmybatis、vue、element-ui

## 2 演示环境
	http://jianli.hzbailing.cn
	admin
	123456

![avatar](https://public-hzjianli.oss-cn-hangzhou.aliyuncs.com/jianli/02.png)


![avatar](https://public-hzjianli.oss-cn-hangzhou.aliyuncs.com/jianli/03.png)


## 3 QQ交流群

	906041180

## 4 搭建开发环境

### 4.1 创建数据库
	创建mysql数据库，然后从document目录jianli.sql脚本文件或jianli.nb3备份文件恢复。
### 4.2 修改dev配置文件
	修改application-dev.yml文件中的配置项：
	datasource
	redis
	upload.localPath

修改完dev配置文件文件后就可以在本地运行程序了

默认本地访问地址是http://localhost:8310

## 5 发布正式环境

	修改application-product.yml文件中的配置项：
	datasource
	redis
	upload.domain
	upload.localPath
	web.domain
	
	在common.js文件中修改前端调用后端接口的配置
	jianli/code/server/web/src/main/resources/static/js/common.js
	
	本地开发时API_DOMAIN用本机地址，正式发布之前改成用正式地址。参考文件开头处的2行代码：
	//let API_DOMAIN = "http://jianli.hzbailing.cn";
	let API_DOMAIN = "http://localhost:8310";

