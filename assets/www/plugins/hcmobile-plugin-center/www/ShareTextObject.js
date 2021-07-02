cordova.define("hcmobile-plugin-center.ShareTextObject", function(require, exports, module) {

    /*
    *  纯文字分享
    *
    *  title (String / 必选) -- 标题，长度依各个平台的要求而定
    *  scene (Int / 可选) -- 场景，不同的平台都有自己的默认值
    *
    *  qq_target_url qq分享必选 跳转的url
    * */

    var ShareTextObject = function (text, scene) {
        this.text = text || '';
        this.scene = scene || ShareScene.UnKnown;

        this.qq_target_url = '';
    };

    /*
     *  检查对象的公共的必选参数
     *
     *  false 表示缺少必选参数
     *
     * */
    ShareTextObject.prototype.hasLegalProperty = function () {
        return !(ShareUtils.isEmptyStringProperty(this.text));
    };

    module.exports = ShareTextObject;



});
