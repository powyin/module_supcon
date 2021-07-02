if (window.self === window.top) {
    // 顶级web页面
    document.write("<script type='text/javascript' src='supconit://cordova.js'></script>");
} else {
    var m_documentEventHandlers = ['pause', 'resume', 'activated', 'deviceready', 'killOffTheLine','updateRecentSession'];

    var m_document_addEventListener = document.addEventListener;
    var m_window_addEventListener = window.addEventListener;
    var m_document_removeEventListener = document.removeEventListener;
    var m_window_removeEventListener = window.removeEventListener;

    // 同步cordova引用
    function prepareCordovaEnv() {
        if (window.is_Prepare_Cordova_Env_Ready) {
            return
        }
        window.is_Prepare_Cordova_Env_Ready = true;

        var navigatorStart = new RegExp("^navigator");
        var windowStart = new RegExp("^window");
        function synFunc(clobItem) {
            if(clobItem === "window"){
                return;
            }

            if (navigatorStart.test(clobItem)) {
                clobItem = clobItem.substr("navigator.".length);
                window.navigator[clobItem] = window.parent.navigator[clobItem];
                // console.log("aaaaaaa navigatorStart  iswork  " + clobItem + "   " + navigator[clobItem]);
            } else {
                if (windowStart.test(clobItem)) {
                    clobItem = clobItem.substr("window.".length);
                }
                window[clobItem] = window.parent[clobItem]
                // console.log("aaaaaaa windowStart  iswork  " + clobItem + "      " + window[clobItem])
            }
        }

        // 提取cordova_plugins信息
        var moduleList = window.parent.cordova.require("cordova/plugin_list");
        for (var i = 0; i < moduleList.length; i++) {
            var clobbers = moduleList[i].clobbers;
            var merges = moduleList[i].merges;
            if (clobbers) {
                for (var j = 0; j < clobbers.length; j++) {
                    var clobItem = clobbers[j];
                    try {
                        synFunc(clobItem);
                    } catch (e) {
                        console.log(e)
                    }
                }
            }
            if(merges){
                for (var k = 0; k < merges.length; k++) {
                    var mergeItem = merges[k];
                    try {
                        synFunc(mergeItem);
                    } catch (e) {
                        console.log(e)
                    }
                }
            }
        }

        window.console = window.parent.console;

    }


    // 防止子iframe未注册deviceready 导致不能用cordova环境
    window.parent.addEventListener("deviceready", prepareCordovaEnv, false);
    window.parent.document.addEventListener("deviceready", prepareCordovaEnv, false);

    window.addEventListener = function (evt, handler, capture) {
        if (evt === "deviceready") {
            window.parent.addEventListener("deviceready", function () {
                prepareCordovaEnv();
                handler();
            }, capture);
        } else {
            if (m_documentEventHandlers.indexOf(evt) !== -1) {
                window.parent.addEventListener(evt, handler, capture);
            } else {
                m_window_addEventListener.call(window, evt, handler, capture);
            }
        }
    };

    document.addEventListener = function (evt, handler, capture) {
        if (evt === "deviceready") {
            window.parent.document.addEventListener("deviceready", function () {
                prepareCordovaEnv();
                handler();
            }, capture);
        } else {
            if (m_documentEventHandlers.indexOf(evt) !== -1) {
                window.parent.document.addEventListener(evt, handler, capture);
            } else {
                m_document_addEventListener.call(document, evt, handler, capture);
            }
        }
    };

    window.removeEventListener = function (evt, handler, capture) {
        // If unsubscribing from an event that is handled by a plugin
        if (m_documentEventHandlers.indexOf(evt) !== -1) {
            window.parent.removeEventListener(evt, handler, capture);
        } else {
            m_window_removeEventListener.call(window, evt, handler, capture);
        }
    };


    document.removeEventListener = function (evt, handler, capture) {
        // If unsubscribing from an event that is handled by a plugin
        if (m_documentEventHandlers.indexOf(evt) !== -1) {
            window.parent.document.removeEventListener(evt, handler, capture);
        } else {
            m_document_removeEventListener.call(document, evt, handler, capture);
        }
    };


}


// // 复制navigator属性
// if (window.navigator) {
//     for (var key in  window.parent.navigator) {
//         console.log("---------------------" + key);
//         try {
//             window.navigator[key] = window.parent.navigator[key];
//         } catch (e) {
//             console.log(e);
//         }
//     }
// }


