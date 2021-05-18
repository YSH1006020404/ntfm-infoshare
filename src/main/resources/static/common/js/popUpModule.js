var lesminiOpen = mini.open;
mini.open = function(cfg) {
    var miniopenConfig = $.extend({
        targetWindow: window,   //页面对象。默认是顶级页面。
        url: "",        //页面地址
        title: "title",      //标题
        width: 1000,      //宽度
        height: 600,     //高度
        allowResize: false,       //允许尺寸调节
        allowDrag: true,         //允许拖拽位置
        showCloseButton: true,   //显示关闭按钮
        showMaxButton: false,     //显示最大化按钮
        showModal: false,         //显示遮罩
        loadOnRefresh: true,       //true每次刷新都激发onload事件
        onload: function () {       //弹出页面加载完成
        },
        ondestroy: function (action) {  //弹出页面关闭前
        }
    },cfg);
    lesminiOpen(miniopenConfig);
};


var popUpModule ={};

popUpModule.fun1={
    url:"userInfo.html",
    width:1000,
    height:600,
    title:"title"
};


popUpModule.user={
    url:"userInfo.html",
    width:1000,
    height:600,
    title:"用户信息"
};