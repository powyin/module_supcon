cordova.define("hcmobile-plugin-center.FilePicker", function(require, exports, module) {
var exec = require('cordova/exec');

exports.open = function (success,error, options) {

    var image = false;
    var video = false;
    var audio = false
    var document = false;

    if (options != null && options != undefined) {

        if (options.image != null && options.image != undefined) {
            image = options.image;
        }
        if (options.video != null && options.video != undefined) {
            video = options.video;
        }
        if (options.audio != null && options.audio != undefined) {
            audio = options.audio;
        }
        if (options.document != null && options.document != undefined) {
            document = options.document;
        }
    }

    exec(success, error, "FilePicker", "open", [image,video,audio,document]);
};
});
