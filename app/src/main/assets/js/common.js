// 表单序列化
function formJson(id){
    var result = {}
    var json = $(id).serializeArray();
    $.each(json, function(){
        if (result[this.name] == undefined) {
            result[this.name] = this.value || '';
        }else {
            if (!result[this.name].push) {
                result[this.name] = [result[this.name]];
            }
            result[this.name].push(this.value || '');
        }
    });
    return result;
}

// post ajax提交
function jsonPost(url, jsonObject, successFunction){
    jsonRequest(url, "post", jsonObject, successFunction, true);
}
// post ajax提交
function jsonPostAsync(url, jsonObject, successFunction){
    jsonRequest(url, "post", jsonObject, successFunction, false);
}

// post ajax提交
function jsonGet(url, jsonObject, successFunction){
    jsonRequest(url, "get", jsonObject, successFunction, true);
}

// ajax提交
function jsonRequest(url, type, jsonObject, successFunction, async){
    $.ajax({
        url:encodeURI(url),
        type:type,
        async:async,
        data:JSON.stringify(jsonObject),
        contentType: 'application/json;charset=utf-8',
        dataType:"json",
        success:function (data) {
            successFunction(data);
        },
        error:function(){
            alert("请求失败");
        }
    });
}

// 获取url中的参数值
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]); return null; //返回参数值
}

// 判断是否为空
function isNull(str){
    if (str==null || str.length==0){
        return true;
    }
    return false;
}