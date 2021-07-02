cordova.define("hcmobile-plugin-center.ShareMusicObject", function(require, exports, module) {


    /*
    *  音乐链接分享
    *
    *  musicUrl (String / 可选) -- 音乐网页的url地址
    *  title (String / 可选) -- 标题，长度依各个平台的要求而定
    *  description (String / 可选) -- 描述
    *  musicDataUrl (String / 可选) -- 音乐数据url地址
    *  scene (Int / 可选) -- 场景
    *  musicLowBandUrl (String / 可选) -- 音乐lowband网页的url地址
    *  musicLowBandDataUrl (String / 可选) -- 音乐lowband数据url地址
    *  thumbImage (String / 可选) -- 缩略图URL，支持 http/https/file/cdvfile/base64，图片大小依各个平台的要求而定
    *  flashUrl (String / 可选) -- 音频URL地址，最长512个字符
    *
    *  musicUrl 不能为空
    *
    *  qq_target_url qq分享必选 跳转的url
    * */
    var ShareMusicObject = function (musicUrl, title, description, thumbImage, scene) {
        this.musicUrl = musicUrl || '';
        this.title = title || '';
        this.description = description || '';
        this.musicDataUrl = '';
        this.thumbImage = thumbImage || '';
        this.scene = scene || ShareScene.UnKnown;
        this.musicLowBandUrl = '';
        this.musicLowBandDataUrl = '';
        this.flashUrl = '';
        this.qq_target_url='';
    };

    ShareMusicObject.prototype.hasLegalProperty = function () {

        for (var p in this) {

            if (!this.hasOwnProperty(p)) continue;

            if (p === 'scene') continue;

            if (ShareUtils.typeNameOf(this[p]) !== 'String') {
                return false;
            }
        }

        return !(ShareUtils.isEmptyStringProperty(this.musicUrl));

    };

    module.exports = ShareMusicObject;





});
