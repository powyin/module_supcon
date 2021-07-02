cordova.define("hcmobile-plugin-developer.HC_Developer", function(require, exports, module) {
    var exec = require('cordova/exec');
    var channel = require('cordova/channel');
    channel.waitForInitialization('onDeveloperFileReady');
    channel.onCordovaReady.subscribe(function () {
        function after(paths) {
            try {
                for (let i = 0; i < paths.length; i++) {
                    let own = paths[i].name;
                    let met = paths[i].methods;
                    try {
                        if (own) {
                            let entry = navigator[own];
                            if (!entry) {
                                entry = {};
                                navigator[own] = entry;
                            }
                            if (met) {
                                for (let j = 0; j < met.length; j++) {
                                    let def = met[j];
                                    entry[def] = function (a, b, c, d, e, f, g) {
                                        exec(a, b, 'HC_Developer', "exc", [own, def, c, d, e, f, g])
                                    }
                                }
                            }
                        }
                    } catch (e) {
                        console.log(e)
                    }

                }
            }catch (e) {
                console.log(e)
            }
            channel.initializationComplete('onDeveloperFileReady');
        }
        exec(after, null, 'HC_Developer', 'hcMobile_getAssertInfo', []);
    });
});
