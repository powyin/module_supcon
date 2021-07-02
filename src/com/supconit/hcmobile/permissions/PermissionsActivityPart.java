package com.supconit.hcmobile.permissions;

import io.reactivex.subjects.PublishSubject;

public interface PermissionsActivityPart {

    boolean containsByPermission(String arg);

    void requestPermissions(String[] permissions);

    boolean isGranted(String permission);

    boolean isRevoked(String permission);

    void setSubjectForPermission(String permission, PublishSubject<Permission> subject);

    PublishSubject<Permission> getSubjectByPermission(String permission);
}
