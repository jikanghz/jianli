//let API_DOMAIN = "http://jianli.hzbailing.cn";
let API_DOMAIN = "http://localhost:8310";

let API_CLIENT = "web 1.0.0";

let vue = null;

axios.interceptors.response.use(res => {
    if (res.data.code && res.data.code > 200) {
        if (res.data.code == 401) {
            if (res.data.message.indexOf("重新") > -1) {
                vue.$alert(res.data.message, '温馨提示', {
                    confirmButtonText: '确定',
                    callback: action => {
                        location.href = '/login';
                    }
                });
            }
        }
        else if (res.data.code == 403) {
            vue.$alert('权限不足', '温馨提示', {
                confirmButtonText: '确定',
                callback: action => {
                }
            });
        }
        else {
            vue.$message({ showClose: true, message: res.data.message, type: 'error' });
        }
    }
    return res;
},
error => {
    vue.$message({
        showClose: true, message:'请求失败，请稍后重试！', type: 'error' });
    return Promise.reject(error);
    }
)


function getApiUrl(service, method) {
    return API_DOMAIN +'/api/post/' + service + '/' + method;
}


function commonList(conditions, method) {

    let data = {};
    if(!method)
    {
        method = "list";
    }

    if(vue.page != null)
    {
        data = {
            page:
                {
                    pageNumber: vue.page.pageNumber,
                    pageSize: vue.page.pageSize,
                    orderBy: vue.page.orderBy,
                },
            conditions: conditions
        }
    }

    let postData = createJsonRequest(data);
    vue.listLoading = true;

    axios.post(getApiUrl(vue.service, method), postData)
        .then(function (response) {
            vue.listLoading = false;
            if (isOK(response)) {
                vue.entityList = response.data.data.entityList;
                vue.afterList(response.data.data);
                if(response.data.data.page == null)
                {
                    return;
                }
                vue.page.totalCount = response.data.data.page.totalCount;
                if (vue.page.totalCount > 0 && vue.page.pageNumber > 1 && vue.page.totalCount <= vue.page.pageSize * (vue.page.pageNumber - 1)) {
                    vue.page.pageNumber = 1;
                    commonList(vue, vue.service, "list", conditions);
                }
            }
        })
}

function commonOnPageSizeChange(val)
{
    vue.page.pageSize = val;
    vue.onList();
}

function commonOnPageNumberChange(val)
{
    vue.page.pageNumber = val;
    vue.onList();
}

function commonOnSortChange(column, prop, order)
{
    if (column.order != null) {
        if (column.order == "ascending") {
            vue.page.orderBy = column.prop + " ASC";
        }
        else {
            vue.page.orderBy = column.prop + " DESC";
        }
        vue.onList();
    }
}

function commonGet(id, data)
{
    let postData = createJsonRequest({
        id: id
    });

    if(data != null)
    {
        data.id = id;
        postData = createJsonRequest(data);
    }

    axios.post(getApiUrl(vue.service, "get"), postData)
        .then(function (response) {
            if (isOK(response)) {
                vue.entity =  response.data.data.entity;
                vue.afterGet(response.data.data);
            }
            vue.listLoading = false;
        })
}

function commonShowInsert(title) {
    if(title == null)
    {
        title = "新建" + vue.displayName;
    }

    vue.get("");
    vue.editTitle = title;
    vue.editVisible = true;
    if (!vue.valid) {
        vue.$refs["entity"].resetFields();
    }
}

function commonShowUpdate(id, title) {
    if(title == null)
    {
        title = "修改" + vue.displayName;
    }
    vue.get(id);
    vue.editTitle = title;
    vue.editVisible = true;
    if (!vue.valid) {
        vue.$refs["entity"].resetFields();
    }
}

function commonShowDetail(id,title){
    if(title == null)
    {
        title = "查看" + vue.displayName;
    }
    vue.get(id);
    vue.editTitle = title;
    vue.editVisible = true;
    if (!vue.valid) {
        vue.$refs["entity"].resetFields();
    }
}

function commonPost(service, method, data)
{
    let postData = createJsonRequest(data);
    axios.post(getApiUrl(service, method), postData)
        .then(function (response) {
            if (isOK((response))) {
                vue.$message({showClose: true, message: "操作成功", type: 'success'});
                vue.onList();
                vue.editVisible = false;
            }
            vue.listLoading = false;
        })
}

function commonSave() {
    vue.$refs["entity"].validate((valid) => {
        if (valid) {
            if(vue.entity.id > 0)
            {
                commonPost(vue.service, "update", vue.entity);
            }
            else
            {
                commonPost(vue.service, "insert", vue.entity);
            }
        }
        else {
            vue.valid = false;
            return false;
        }
    });
}

function commonDelete(id, title) {
    if(title == null)
    {
        title = "确定要删除选中的" + vue.displayName + "吗？";
    }
    vue.$confirm(title, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    }).then(() => {
        commonPost(vue.service, "delete", {id: id});
    }).catch(() => {
    });
}

function commonExportList(conditions) {
    let postData = createJsonRequest({
        conditions: conditions
    });
    let enData = encodeURIComponent(Base64.encode(JSON.stringify(postData)));

    let url =  API_DOMAIN +'/api/export/' + vue.service + '/' + "export/?jsonRequest=" + enData;

    document.location.href = url;
}

function createCondition(conditions, fieldName, operator, value, type) {
    if (value != null && value != "") {
                 if (type == "dateRange") {
                     if (value.length == 2) {
                         let startItem = {
                             "fieldName": fieldName,
                             "operator": ">=",
                             "condition": getDate(value[0])
                         };
                         conditions.push(startItem);
                         let endItem = {
                             "fieldName": fieldName,
                             "operator": "<",
                             "condition": getNextDay(value[1]),
                         };
                         conditions.push(endItem);
                     }
                     return;
                 }

        if (operator == "LIKE") {
            value = "%" + value + "%";

        }
        if (fieldName.indexOf(",") != -1 ){
            let item = {
                "fieldName": fieldName,
                "operator": operator,
                "condition": value,
                "isParam":false
            };
            conditions.push(item);
            return;
        }

        if (operator == "IN"){
            let item = {
                "fieldName": fieldName,
                "operator": operator,
                "condition": value,
                "isParam":false
            };
            conditions.push(item);
            return;
        }
        let item = {
            "fieldName": fieldName,
            "operator": operator,
            "condition": value
        };
        conditions.push(item);
    }
}

function getNextDay(d) {
    d = new Date(d);
    d = +d + 1000 * 60 * 60 * 24;
    d = new Date(d);
    return d.getFullYear() + "-" + (d.getMonth() + 1) + "-" + d.getDate();
}


function isOK(response) {
    if(response.data.code == "200")
    {
        return true;
    }
    return false;
}

function createJsonRequest(data) {
    if(data == null)
    {
        data = {};
    }
    let jsonRequest =
        {
            client: API_CLIENT,
            token: getCookie("apiToken"),
            data: data
        }
     return    jsonRequest;
}


function getCookie(name) {
    let arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));
    if (arr != null) {
        return arr[2];
    }
    return null;
}

function setCookie(name, value, expiredays){
    if(expiredays == null)
    {
        expiredays = 1;
    }
    let exdate = new Date();
    exdate.setDate(exdate.getDate() + expiredays);
    document.cookie = name + "=" + escape(value) + ";expires=" + exdate.toGMTString();
}



function getDate(d) {
    if(d == null || d.length < 1)
    {
        return "";
    }
    var date = new Date(d);
    var year = date.getFullYear();
    var month = (date.getMonth() + 1 < 10)? '0' + (date.getMonth() + 1): date.getMonth() + 1;
    var day = (date.getDate() < 10)? '0' + date.getDate(): date.getDate();
    return year + "-" + month + "-" + day ;
}

function getLocalDate() {
    return new Date((new Date().getFullYear() + "-" + (new Date().getMonth() + 1) + "-" + new Date().getDate()).replace(/-/g, '/'));
}



function getDateTime(d) {
    if(d == null || d.length < 1)
    {
        return "";
    }
    var date = new Date(d);
    var year = date.getFullYear();
    var month = (date.getMonth() + 1 < 10)? ('0' + (date.getMonth() + 1)) : (date.getMonth() + 1);
    var day = (date.getDate() < 10)? '0' + date.getDate() : date.getDate();
    var hh = (date.getHours() < 10)? '0' + date.getHours() : date.getHours();
    var mm = (date.getMinutes() < 10)? '0' + date.getMinutes(): date.getMinutes();
    var ss = (date.getSeconds() < 10)? '0' + date.getSeconds(): date.getSeconds();
    return year + "-" + month + "-" + day + " " + hh + ":" + mm + ":" + ss;
}

function getCodeName(codeTable, codeValue) {
    if(codeTable == null || codeTable.length < 1)
    {
        return "";
    }
    if(codeValue == null || codeValue.length < 1)
    {
        return "";
    }
   for(let i=0; i<codeTable.length; ++i)
    {
        if(codeTable[i].codeValue == codeValue)
        {
            return codeTable[i].codeName;
        }
    }
}

function getUrlParameter(name) {
    var url = window.location.search;
    if (url.indexOf('?') == 1) { return false; }
    url = url.substr(1);
    url = url.split('&');
    var name = name || '';
    var object;
    // 获取全部参数及其值
    for (var i = 0; i < url.length; i++) {
        var info = url[i].split('=');
        var obj = {};
        obj[info[0]] = decodeURI(info[1]);
        url[i] = obj;
    }
    // 如果传入一个参数名称，就匹配其值
    if (name) {
        for (var i = 0; i < url.length; i++) {
            for (const key in url[i]) {
                if (key.toLowerCase() == name.toLowerCase()) {
                    object = url[i][key];
                }
            }
        }
    } else {
        object = url;
    }
    // 返回结果
    return object;
}

function addUrlParameter(url, name, value) {
    if(url.indexOf('?') < 0){
        url += "?";
    }
    else{
        url += "&";
    }
    url += name + "=" + value;
    return url;
}

function commonLang(){
    lang = getCookie("lang");
    if(lang == null || lang=="")
    {
        lang = "cn";
    }

    if(lang == "cn")
    {
        return language_cn;
    }
    else if(lang == "en")
    {
        return language_en;
    }
}