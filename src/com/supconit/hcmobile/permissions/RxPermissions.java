/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.supconit.hcmobile.permissions;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

public class RxPermissions {
    private static final Object TRIGGER = new Object();
    private PermissionsActivityPart mRxPermissionsFragment;
    public RxPermissions(@NonNull PermissionsActivityPart activity) {
        mRxPermissionsFragment = activity;
    }

    public Observable<Permission> requestEach(final String... permissions) {
        return Observable.just(TRIGGER).compose(new ObservableTransformer<Object, Permission>() {
            @Override
            public ObservableSource<Permission> apply(Observable<Object> o) {
                return request(o, permissions);
            }
        });
    }

    private Observable<Permission> request(final Observable<?> trigger, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("RxPermissions.request/requestEach requires at least one input permission");
        }
        return oneOf(trigger, pending(permissions))
                .flatMap(new Function<Object, Observable<Permission>>() {
                    @Override
                    public Observable<Permission> apply(Object o) {
                        return requestImplementation(permissions);
                    }
                });
    }

    private Observable<?> pending(final String... permissions) {
        for (String p : permissions) {
            if (!mRxPermissionsFragment.containsByPermission(p)) {
                return Observable.empty();
            }
        }
        return Observable.just(TRIGGER);
    }

    private Observable<?> oneOf(Observable<?> trigger, Observable<?> pending) {
        if (trigger == null) {
            return Observable.just(TRIGGER);
        }
        return Observable.merge(trigger, pending);
    }


    private Observable<Permission> requestImplementation(final String... permissions) {
        List<Observable<Permission>> list = new ArrayList<>(permissions.length);
        List<String> unrequestedPermissions = new ArrayList<>();

        for (String permission : permissions) {
            Log.d("ask permission:", "Requesting permission " + permission);
            if (isGranted(permission)) {
                list.add(Observable.just(new Permission(permission, true, false)));
                continue;
            }

            if (isRevoked(permission)) {
                list.add(Observable.just(new Permission(permission, false, false)));
                continue;
            }

            PublishSubject<Permission> subject = mRxPermissionsFragment.getSubjectByPermission(permission);
            if (subject == null) {
                unrequestedPermissions.add(permission);
                subject = PublishSubject.create();
                mRxPermissionsFragment.setSubjectForPermission(permission, subject);
            }
            list.add(subject);
        }

        if (!unrequestedPermissions.isEmpty()) {
            String[] unrequestedPermissionsArray = unrequestedPermissions.toArray(new String[0]);
            Log.d("ask permission:", "requestPermissionsFromFragment " + TextUtils.join(", ", unrequestedPermissionsArray));
            mRxPermissionsFragment.requestPermissions(unrequestedPermissionsArray);
        }

        return Observable.concat(Observable.fromIterable(list));
    }

    @SuppressWarnings("WeakerAccess")
    public Observable<Boolean> shouldShowRequestPermissionRationale(final Activity activity, final String... permissions) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            return Observable.just(false);
        }
        return Observable.just(shouldShowRequestPermissionRationaleImplementation(activity, permissions));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationaleImplementation(final Activity activity, final String... permissions) {
        for (String p : permissions) {
            if (!isGranted(p) && !activity.shouldShowRequestPermissionRationale(p)) {
                return false;
            }
        }
        return true;
    }

    private boolean isGranted(String permission) {
        return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) || mRxPermissionsFragment.isGranted(permission);
    }

    private boolean isRevoked(String permission) {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && mRxPermissionsFragment.isRevoked(permission);
    }

}







