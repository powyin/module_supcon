cordova.define("hcmobile-plugin-center.MobileCenter", function(require, exports, module) {
var exec = require('cordova/exec');

exports.reset = function () {
    exec(null, null, "Center", "reset", []);
};

exports.reload = function () {
    exec(null, null, "Center", "reload", []);
};

exports.getStatusBarHeight = function (callback) {
    exec(callback, null, "Center", "getStatusBarHeight", []);
};
});
