cordova.define("hcmobile-plugin-debug.Debugger", function(require, exports, module) {
var exec = require('cordova/exec');

/** 外部浏览器打开链接 */
exports.getlog = function (success, error) {
    exec(success, error, "Debugger", "getlog", []);
}

exports.getDebuggingAddress = function (success, error) {
    exec(success, error, "Debugger", "getDebuggingAddress", []);
}

exports.loadDebugConfig = function () {
    exec(null, null, "Debugger", "loadDebugConfig", []);
}

exports.enableDebugLog = function (enable) {
    exec(null, null, "Debugger", "enableDebugLog", [enable]);
}

});
