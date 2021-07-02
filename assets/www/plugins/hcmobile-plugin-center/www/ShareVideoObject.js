cordova.define("hcmobile-plugin-center.ShareVideoObject", function(require, exports, module) {

    /*
    *  音乐链接分享
    *
    *  videoUrl (String / 可选) -- 音乐网页的url地址
    *  title (String / 可选) -- 标题，长度依各个平台的要求而定
    *  description (String / 可选) -- 描述
    *  musicDataUrl (String / 可选) -- 音乐数据url地址
    *  thumbImage (String / 可选) -- 缩略图URL，支持 http/https/file/cdvfile/base64，图片大小依各个平台的要求而定
    *  flashUrl (String / 可选) -- 音频URL地址，最长512个字符
    *
    *  videoUrl 不能为空
    *    
    *  qq_target_url qq分享必选 跳转的url
    *
    * */

    var ShareVideoObject = function (videoUrl, title, description, thumbImage, scene) {
        this.videoUrl = videoUrl || '';
        this.title = title || '';
        this.description = description || '';
        this.thumbImage = thumbImage || '';
        this.scene = scene || ShareScene.UnKnown;
        this.flashUrl = '';
        this.videLowBandUrl = '';
        // this.videoStreamUrl = '';
        // this.videLowBandStreamUrl = '';

        this.qq_target_url='';
    };


    ShareVideoObject.prototype.hasLegalProperty = function () {

        for (var p in this) {

            if (!this.hasOwnProperty(p)) continue;

            if (p === 'scene') continue;

            if (ShareUtils.typeNameOf(this[p]) !== 'String') {
                return false;
            }
        }

        return !(ShareUtils.isEmptyStringProperty(this.videoUrl));

    };

    module.exports = ShareVideoObject;




});
