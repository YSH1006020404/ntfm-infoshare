/**
 *关闭弹窗
 */
function CloseWindow(action) {
    if (action == "close" && form.isChanged()) {
        if (mini.confirm("数据被修改了，是否先保存？")) {
            return false;
        }
    }
    if (window.CloseOwnerWindow) {
        return window.CloseOwnerWindow(action);
    } else {
        window.close();
    }
}

/**
 *取消关闭弹窗
 */
function onCancel(e) {
    CloseWindow("cancel");
}

/**********************页面渲染*************************/
/**
 *渲染字段是否为空
 */
function onIsNoRenderer(e) {
    var isNoData = [{id: "1", value: "1", text: '是'}, {id: "0", value: "0", text: '否'}];
    for (var i = 0, l = isNoData.length; i < l; i++) {
        var g = isNoData[i];
        if (g.id == "" + e.value) return g.text;
    }
    return e.value;
}

/**
 *渲染字段的开关状态
 */
function onOnOffRenderer(e) {
    var onOffData = [{id: "1", value: "1", text: '开启'}, {id: "0", value: "0", text: '关闭'}];
    for (var i = 0, l = onOffData.length; i < l; i++) {
        var g = onOffData[i];
        if (g.id == "" + e.value) return g.text;
    }
    return e.value;
}

/**
 * 获取指定表的所有字段
 */
function getTableColumns(tableId, columnCombobox) {
    $.callJson({
        url: "/infoshareConf/common/listColumns",
        data: {"tableId": tableId},
        success: function (data) {
            columnCombobox.setData(data);
            //数据名称只能从列表中选择
            columnCombobox.setValueFromSelect(true);
        },
        error: function (e) {
            mini.alert("检索输出字段失败！");
        }
    });
}
