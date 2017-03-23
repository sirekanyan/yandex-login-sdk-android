package com.yandex.yaloginsdk.internal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class ActivityStarter {

    @Nullable
    private final Fragment fragment;

    @Nullable
    private final FragmentActivity activity;

    public ActivityStarter(@Nullable final Fragment fragment) {
        this.fragment = fragment;
        this.activity = null;
    }

    public ActivityStarter(@Nullable final FragmentActivity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    public void startActivityForResult(@NonNull final Intent intent, final int requestCode) {
        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        } else if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            throw new IllegalStateException("Either activity or fragment should be set!");
        }
    }

    public void showDialogFragment(@NonNull final DialogFragment dialog) {
        final String tag = dialog.getClass().getName();
        if (activity != null) {
            dialog.show(activity.getSupportFragmentManager(), tag);
        } else if (fragment != null) {
            dialog.setTargetFragment(fragment, 0);
            dialog.show(fragment.getActivity().getSupportFragmentManager(), tag);
        } else {
            throw new IllegalStateException("Either activity or fragment should be set!");
        }
    }

    @NonNull
    public Context getContext() {
        if (activity != null) {
            return activity;
        } else if (fragment != null) {
            return fragment.getActivity();
        } else {
            throw new IllegalStateException("Either activity or fragment should be set!");
        }
    }
}