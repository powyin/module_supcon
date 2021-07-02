package com.supconit.hcmobile.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.MapsInitializer;
import com.supconit.hcmobile.HcmobileApp;

import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeObserver;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * 获取 Android api 提供的 手机位置坐标 每隔 10s 更新一次
 */
public class LocationFinder {
    @SuppressLint("StaticFieldLeak")
    private static LocationFinder androidLocation;
    private static int isConfig;

    static LocationFinder getInstance(Context context) {
        if (androidLocation == null) {
            androidLocation = new LocationFinder();
        }
        return androidLocation;
    }

    private LocationManager mLocationManager;
    private PublishSubject<Location> publishSubject = PublishSubject.create();
    private Location mPosition;
    private AMapLocation mapLocation;

    private Maybe<Location> gps = Maybe.create(new MaybeOnSubscribe<Location>() {
        @SuppressLint("MissingPermission")
        @Override
        public void subscribe(MaybeEmitter<Location> emitter) throws Exception {
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mPosition = location;
                    emitter.onSuccess(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, Looper.getMainLooper());
        }
    });

    private Maybe<Location> net = Maybe.create(new MaybeOnSubscribe<Location>() {
        @SuppressLint("MissingPermission")
        @Override
        public void subscribe(MaybeEmitter<Location> emitter) throws Exception {
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mPosition = location;
                    emitter.onSuccess(location);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            }, Looper.getMainLooper());
        }
    });

    @SuppressLint("MissingPermission")
    private LocationFinder() {
        mLocationManager = (LocationManager) HcmobileApp.getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }

    private boolean canLocation() {
        boolean gps = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    private void openGPS() {
        Intent GPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        GPSIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            HcmobileApp.getApplication().startActivity(GPSIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // todo 让用户打开gps设置页面 最大可能请求gps
    public Single<Location> getLocation() {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            private final Object mLock = new Object();
            boolean wait = false;

            @Override
            public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                if (canLocation()) {
                    emitter.onSuccess(wait);
                } else {
                    HcmobileApp.getHandle().post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(HcmobileApp.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            builder.setMessage("为保障程序正常使用\n需要开启gps服务");
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    synchronized (mLock) {
                                        mLock.notifyAll();
                                    }
                                }
                            });
                            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int type) {
                                    wait = true;
                                    dialogInterface.dismiss();
                                    openGPS();
                                }
                            });
                            builder.show();
                        }
                    });
                    synchronized (mLock) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    emitter.onSuccess(wait);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(new Function<Boolean, SingleSource<Location>>() {
            @Override
            public SingleSource<Location> apply(Boolean aBoolean) throws Exception {
                final long extWait = aBoolean ? 60 * 3 : 0;
                return publishSubject.firstOrError().timeout(6 + extWait, TimeUnit.SECONDS).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        gps.timeout(5 + extWait, TimeUnit.SECONDS).subscribe(new MaybeObserver<Location>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onSuccess(Location location) {
                                mPosition = location;
                                publishSubject.onNext(location);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                        net.timeout(5 + extWait, TimeUnit.SECONDS).subscribe(new MaybeObserver<Location>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onSuccess(Location location) {
                                mPosition = location;
                                publishSubject.onNext(location);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });


                        if (isConfig == 0) {
                            String saveKey = Util.getCordovaConfigTag("gaodeMapKeyAndroid", "value");
                            if (!TextUtils.isEmpty(saveKey)) {
                                isConfig = 1;
                                MapsInitializer.setApiKey(saveKey);
                                AMapLocationClient.setApiKey(saveKey);
                            } else {
                                isConfig = -1;
                            }
                        }

                        if (isConfig == 1) {
                            AMapLocationClient mLocationClient = new AMapLocationClient(HcmobileApp.getApplication());
                            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                            mLocationClient.setLocationListener(new AMapLocationListener() {
                                @Override
                                public void onLocationChanged(AMapLocation amapLocation) {
                                    Log.e("powyin", "gps gd onLocationChanged " + (amapLocation == null ? "null" : amapLocation.getLatitude()));
                                    if (amapLocation != null && amapLocation.getLatitude() != 0) {
                                        mapLocation = amapLocation;
                                    }
                                }
                            });
                            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                            mLocationOption.setOnceLocation(true);
                            mLocationClient.setLocationOption(mLocationOption);
                            mLocationClient.startLocation();
                        }

                    }
                }).onErrorReturn(new Function<Throwable, Location>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public Location apply(Throwable throwable) throws Exception {

                        if (mapLocation != null) {
                            Location location = new Location("");
                            location.setLatitude(mapLocation.getLatitude());
                            location.setLongitude(mapLocation.getLongitude());
                            return location;
                        }

                        if (mPosition == null) {
                            try {
                                mPosition = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (mPosition != null) {
                            return mPosition;
                        } else {
                            throw new RuntimeException("no cache location for use");
                        }
                    }
                });
            }
        });


    }


    // todo 让用户打开gps设置页面 最大可能请求gps  精度优先
    public Single<Location> getLocationHighAccuracy() {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            private final Object mLock = new Object();
            boolean wait = false;

            @Override
            public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                if (canLocation()) {
                    emitter.onSuccess(wait);
                } else {
                    HcmobileApp.getHandle().post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(HcmobileApp.getActivity(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
                            builder.setMessage("为保障程序正常使用\n需要开启gps服务");
                            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    synchronized (mLock) {
                                        mLock.notifyAll();
                                    }
                                }
                            });
                            builder.setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int type) {
                                    wait = true;
                                    dialogInterface.dismiss();
                                    openGPS();
                                }
                            });
                            builder.show();
                        }
                    });
                    synchronized (mLock) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    emitter.onSuccess(wait);
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).flatMap(new Function<Boolean, SingleSource<Location>>() {
            @Override
            public SingleSource<Location> apply(Boolean aBoolean) throws Exception {
                final long extWait = aBoolean ? 60 * 3 : 0;
                return publishSubject.firstOrError().timeout(6 + extWait, TimeUnit.SECONDS).doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        gps.timeout(5 + extWait, TimeUnit.SECONDS).subscribe(new MaybeObserver<Location>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onSuccess(Location location) {
                                mPosition = location;
                                publishSubject.onNext(location);
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });

                        net.timeout(5 + extWait, TimeUnit.SECONDS).subscribe(new MaybeObserver<Location>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                            }

                            @Override
                            public void onSuccess(Location location) {
                                mPosition = location;
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {
                            }
                        });


                        if (isConfig == 0) {
                            String saveKey = Util.getCordovaConfigTag("gaodeMapKeyAndroid", "value");
                            if (!TextUtils.isEmpty(saveKey)) {
                                isConfig = 1;
                                MapsInitializer.setApiKey(saveKey);
                                AMapLocationClient.setApiKey(saveKey);
                            } else {
                                isConfig = -1;
                            }
                        }

                        if (isConfig == 1) {
                            AMapLocationClient mLocationClient = new AMapLocationClient(HcmobileApp.getApplication());
                            AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
                            mLocationClient.setLocationListener(new AMapLocationListener() {
                                @Override
                                public void onLocationChanged(AMapLocation amapLocation) {
                                    Log.e("powyin", "gps gd onLocationChanged " + (amapLocation == null ? "null" : amapLocation.getLatitude()));
                                    if (amapLocation != null && amapLocation.getLatitude() != 0) {
                                        mapLocation = amapLocation;
                                    }
                                }
                            });
                            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                            mLocationOption.setOnceLocation(true);
                            mLocationClient.setLocationOption(mLocationOption);
                            mLocationClient.startLocation();
                        }

                    }
                }).onErrorReturn(new Function<Throwable, Location>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public Location apply(Throwable throwable) throws Exception {

                        if (mapLocation != null) {
                            Location location = new Location("");
                            location.setLatitude(mapLocation.getLatitude());
                            location.setLongitude(mapLocation.getLongitude());
                            return location;
                        }

                        if (mPosition == null) {
                            try {
                                mPosition = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (mPosition != null) {
                            return mPosition;
                        } else {
                            throw new RuntimeException("no cache location for use");
                        }
                    }
                });
            }
        });


    }

}
