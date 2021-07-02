cordova.define("hcmobile-plugin-center.ShareImageObject", function(require, exports, module) {


    /*
    *  纯图片分享（支持移动端本地图片和网上图片）
    *
    *  title (String / 必选) -- 标题，长度依各个平台的要求而定
    *  shareImage (String / 可选) -- 图片URL，支持 http/https/file/cdvfile/base64，图片大小依各个平台的要求而定
    *  scene (Int / 可选) -- 场景
    *  thumbImage (String / 可选) -- 缩略图URL，支持 http/https/file/cdvfile/base64，图片大小依各个平台的要求而定
    *  shareImageArray (StringArray / 可选) -- 图片URL数组，支持 http/https/file/cdvfile/base64，图片大小依各个平台的要求而定
    *
    *  shareImage、thumbImage、shareImageArray 三者不能同时为空
    *
    * */
    var ShareImageObject = function (shareImage, thumbImage, scene) {
        this.shareImage = shareImage || '';
        this.scene = scene || ShareScene.UnKnown;
        this.thumbImage = thumbImage || '';
        this.shareImageArray = [];
    };

    ShareImageObject.prototype.hasLegalProperty = function () {

        if (this.scene === ShareScene.Sina){
            // 微博支持多图和单图分享
            if(ShareUtils.isEmptyArrayProperty(this.shareImageArray) || this.shareImageArray.length <= 1 ){
                // 单图
                return !(ShareUtils.isEmptyStringProperty(this.shareImage) && ShareUtils.isEmptyArrayProperty(this.shareImageArray));
            } else{
                return true;
            }
        } else {
            return !(ShareUtils.isEmptyStringProperty(this.shareImage) && ShareUtils.isEmptyStringProperty(this.thumbImage) && ShareUtils.isEmptyArrayProperty(this.shareImageArray));
        }

    };

    module.exports = ShareImageObject;




});
