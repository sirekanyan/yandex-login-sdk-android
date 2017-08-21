package com.yandex.authsdk.sample;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthSdk;
import com.yandex.authsdk.YandexAuthToken;

import java.util.Arrays;

public class MainFragment extends Fragment {

    private static final int REQUEST_LOGIN_SDK = 1;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView tokenLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private TextView jwtLabel;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private YandexAuthSdk sdk;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private View jwtContainer;

    @Nullable
    private YandexAuthToken yandexAuthToken;

    @Nullable
    private String jwt;

    public MainFragment() {
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final View loginButton = view.findViewById(R.id.login);
        loginButton.setOnClickListener(v ->
                startActivityForResult(sdk.createLoginIntent(getActivity(), null), REQUEST_LOGIN_SDK));
        final View jwtButton = view.findViewById(R.id.jwt);
        jwtButton.setOnClickListener(v -> getJwt());

        tokenLabel = (TextView) view.findViewById(R.id.status_label);
        jwtLabel = (TextView) view.findViewById(R.id.jwt_label);
        jwtContainer = view.findViewById(R.id.jwt_container);

        sdk = new YandexAuthSdk(new YandexAuthOptions(getContext(), true));

        if (yandexAuthToken != null) {
            onTokenReceived(yandexAuthToken);
        }
        if (jwt != null) {
            onJwtReceived(jwt);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == REQUEST_LOGIN_SDK) {
            try {
                final YandexAuthToken yandexAuthToken = sdk.extractToken(resultCode, data);
                if (yandexAuthToken != null) {
                    onTokenReceived(yandexAuthToken);
                }
            } catch (YandexAuthException e) {
                tokenLabel.setText(e.getLocalizedMessage());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onTokenReceived(@NonNull YandexAuthToken yandexAuthToken) {
        this.yandexAuthToken = yandexAuthToken;
        tokenLabel.setText(yandexAuthToken.toString());
        jwtContainer.setVisibility(View.VISIBLE);
    }

    private void onJwtReceived(@NonNull String jwt) {
        this.jwt = jwt;
        jwtLabel.setText(jwt);
    }

    private void getJwt() {
        final DialogFragment dialog = new ProgressDialogFragment();
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), ProgressDialogFragment.TAG);

        assert yandexAuthToken != null;

        new Thread(() -> {
            try {
                final String jwt = sdk.getJwt(yandexAuthToken);
                getActivity().runOnUiThread(() -> {
                    onJwtReceived(jwt);
                    dismissProgress();
                });
            } catch (YandexAuthException e) {
                getActivity().runOnUiThread(() -> {
                    jwtLabel.setText(Arrays.toString(e.getErrors()));
                    dismissProgress();
                });
            }

        }).start();
    }

    private void dismissProgress() {
        final Fragment dialogFragment = getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        if (dialogFragment != null) {
            ((DialogFragment) dialogFragment).dismiss();
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        private static final String TAG = ProgressDialogFragment.class.getCanonicalName();

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Waiting");
            dialog.setCancelable(false);
            return dialog;
        }
    }
}