;(function () {
    var require, define;
    (function () {
        var modules = {},
            requireStack = [],
            inProgressModules = {},
            SEPARATOR = ".";

        function build(module) {
            var factory = module.factory,
                localRequire = function (id) {
                    var resultantId = id;
                    if (id.charAt(0) === ".") {
                        resultantId = module.id.slice(0, module.id.lastIndexOf(SEPARATOR)) + SEPARATOR + id.slice(2);
                    }
                    return require(resultantId);
                };
            module.exports = {};
            delete module.factory;
            factory(localRequire, module.exports, module);
            return module.exports;
        }

        require = function (id) {
            if (!modules[id]) {
                throw "module " + id + " not found";
            } else if (id in inProgressModules) {
                var cycle = requireStack.slice(inProgressModules[id]).join('->') + '->' + id;
                throw "Cycle in require graph: " + cycle;
            }
            if (modules[id].factory) {
                try {
                    inProgressModules[id] = requireStack.length;
                    requireStack.push(id);
                    return build(modules[id]);
                } finally {
                    delete inProgressModules[id];
                    requireStack.pop();
                }
            }
            return modules[id].exports;
        };

        define = function (id, factory) {
            if (modules[id]) {
                throw "module " + id + " already defined";
            }

            modules[id] = {
                id: id,
                factory: factory
            };
        };

        define.remove = function (id) {
            delete modules[id];
        };

        define.moduleMap = modules;
    })();

    var glo = global;
    var cordova = {}
    cordova.define = define;
    cordova.require = require;
    cordova.holder = [];
    global.cordova = cordova;
    var navigator = {};
    global.navigator = navigator;


    global.addEventListener = function (id, method) {
        if (!cordova.holder[id]) {
            cordova.holder[id] = new Array();
        }
        cordova.holder[id].push(method);

    }
    global.getEventListener = function (id) {
        if (!cordova.holder[id]) {
            cordova.holder[id] = new Array();
        }
        return cordova.holder[id];
    }


    define("cordova/exec", function (require, exports, module) {
        function exec(successCallback, errorCallback, className, methodName, jsonArray) {
            console.log("excute->: " + className + "   " + methodName + "    " + jsonArray)
            weex.requireModule('modal_weex_access_cordova').excute(className, methodName, jsonArray, function (info) {
                if (info && info.success) {
                    successCallback(info.data);
                } else {
                    if(info && info.data){
                        errorCallback(info.data);
                    }else {
                        errorCallback();
                    }
                }
            })
        }
        module.exports = exec;
    })

    define("cordova/utils", function (require, exports, module) {
    });

    define("cordova/channel", function (require, exports, module) {
    });

    define("cordova/argscheck", function (require, exports, module) {
    });

    define("cordova/platform", function (require, exports, module) {
    });

    define("cordova/pluginloader", function (require, exports, module) {
        exports.injectScript = function (url, onload, onerror) {
            weex.requireModule('modal_weex_access_cordova').getFileStringContent(url, function (info) {
                try {
                    eval(info);
                } catch (e) {
                    console.log(e);
                }
                onload()
            })
        };

        function injectIfNecessary(id, url, onload, onerror) {
            onerror = onerror || onload;
            if (id in define.moduleMap) {
                onload();
            } else {
                exports.injectScript(url, function () {
                    if (id in define.moduleMap) {
                        onload();
                    } else {
                        onerror();
                    }
                }, onerror);
            }
        }

        function onScriptLoadingComplete(moduleList, finishPluginLoading) {
            // Loop through all the plugins and then through their clobbers and merges.
            for (var i = 0, module; module = moduleList[i]; i++) {
                if (module.clobbers && module.clobbers.length) {
                    for (var j = 0; j < module.clobbers.length; j++) {
                        var cmd = module.clobbers[j] + " = require('" + module.id + "')";    //module.clobbers[j] + "={}; " +
                        try {
                            eval(cmd);
                        } catch (info) {
                            console.log(info);
                        }
                        console.log("::::::::::::::::::::; " + cmd);
                    }
                }
            }
            finishPluginLoading();
        }

        function handlePluginsObject(path, moduleList, finishPluginLoading) {
            // Now inject the scripts.
            var scriptCounter = moduleList.length;

            if (!scriptCounter) {
                finishPluginLoading();
                return;
            }

            function scriptLoadedCallback() {
                if (!--scriptCounter) {
                    onScriptLoadingComplete(moduleList, finishPluginLoading);
                }
            }

            for (var i = 0; i < moduleList.length; i++) {
                injectIfNecessary(moduleList[i].id, path + moduleList[i].file, scriptLoadedCallback);
            }
        }

        exports.load = function (callback) {
            injectIfNecessary('cordova/plugin_list', 'cordova_plugins.js',
                function () {
                    var moduleList = require("cordova/plugin_list");
                    handlePluginsObject("", moduleList, callback);
                },
                callback);
        };
    });


    require("cordova/pluginloader").load(function (info) {
        var list = global.getEventListener("deviceready");
        for (let i = 0; i < list.length; i++) {
            var obj = list[i];
            obj();
        }
    })
})();
