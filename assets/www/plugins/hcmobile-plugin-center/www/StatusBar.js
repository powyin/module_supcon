cordova.define("hcmobile-plugin-center.StatusBar", function(require, exports, module) {
var exec = require('cordova/exec');
var argscheck = require('cordova/argscheck');

exports.setColor = function (successCallback, errorCallback , color ) {
    argscheck.checkArgs('FFs', 'statusbar.setColor', arguments);
    exec(successCallback, errorCallback, "Center", "setColor", [color]);
};


});
