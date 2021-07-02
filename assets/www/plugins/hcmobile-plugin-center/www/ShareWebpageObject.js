cordova.define("hcmobile-plugin-center.ShareWebpageObject", function(require, exports, module) {


    var ShareWebpageObject = function (webpageUrl, title, description, thumbImage, scene) {
        this.webpageUrl = webpageUrl || '';
        this.title = title || '';
        this.description = description || '';
        this.thumbImage = thumbImage || '';
        this.scene = scene || ShareScene.UnKnown;

    };


    ShareWebpageObject.prototype.hasLegalProperty = function () {

        for (var p in this) {

            if (!this.hasOwnProperty(p)) continue;

            if (p === 'scene') continue;

            if (ShareUtils.typeNameOf(this[p]) !== 'String') {
                return false;
            }
        }

        return  !(ShareUtils.isEmptyStringProperty(this.webpageUrl));


    };

    module.exports = ShareWebpageObject;



});
