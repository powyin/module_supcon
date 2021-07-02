cordova.define("hcmobile-plugin-center.ShareUtils", function(require, exports, module) {

    var utils = require('cordova/utils');


    ShareObjectType = {
        UnKnown: -1,
        Text: 0,
        Image: 1,
        Video: 2,
        Music: 3
    }


    function shareObjectTypeOf(obj) {

        if (utils.typeName(obj) !== 'Object') return ShareObjectType.UnKnown;

        if (isTextObject(obj)) return ShareObjectType.Text;

        if (isImageObject(obj)) return ShareObjectType.Image;

        if (isMusicObject(obj)) return ShareObjectType.Music;

        if (isVideoObject(obj)) return ShareObjectType.Video;

        if (isWebpageObject(obj)) return ShareObjectType.Webpage;

        return ShareObjectType.UnKnown;
    }


    function isTextObject(obj) {
        return obj instanceof ShareTextObject
    }

    function isImageObject(obj) {
        return obj instanceof ShareImageObject
    }

    function isVideoObject(obj) {
        return obj instanceof ShareVideoObject
    }

    function isMusicObject(obj) {
        return obj instanceof ShareMusicObject
    }

    function isWebpageObject(obj) {
        return obj instanceof ShareWebpageObject
    }

    function isEmptyStringProperty(str) {
        return (str === '' || str === null || str === undefined || utils.typeName(str) !== 'String')
    }

    function isEmptySceneProperty(scene) {
        return (scene === null || scene === undefined || utils.typeName(num) !== 'Number')
    }

    function isEmptyArrayProperty(array) {
        return (array === null || array === undefined || !utils.isArray(array) || !array.length)
    }

    function typeNameOf(obj) {
        return utils.typeName(obj);
    }


    module.exports = { }

            module.exports.ShareObjectType = ShareObjectType
            module.exports.shareObjectTypeOf = shareObjectTypeOf
            module.exports.isTextObject = isTextObject
            module.exports.isImageObject = isImageObject
            module.exports.isVideoObject = isVideoObject
            module.exports.isMusicObject = isMusicObject
            module.exports.isWebpageObject = isWebpageObject
            module.exports.isEmptyStringProperty = isEmptyStringProperty
            module.exports.isEmptySceneProperty = isEmptySceneProperty
            module.exports.isEmptyArrayProperty = isEmptyArrayProperty
            module.exports.typeNameOf = typeNameOf



});
