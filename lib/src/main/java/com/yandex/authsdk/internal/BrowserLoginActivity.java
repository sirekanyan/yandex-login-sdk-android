package com.yandex.authsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthOptions;

import java.util.UUID;

import static com.yandex.authsdk.internal.Constants.EXTRA_OPTIONS;

public class BrowserLoginActivity extends Activity {

    public static final String EXTRA_BROWSER_PACKAGE_NAME = "com.yandex.authsdk.internal.EXTRA_BROWSER_PACKAGE_NAME";

    public static final String STATE_LOADING_STATE = "com.yandex.authsdk.STATE_LOADING_STATE";

    private enum State {
        INITIAL, TOKEN_REQUESTED, TOKEN_LOADED;
    }

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private ExternalLoginHandler loginHandler;

    @NonNull
    private State state = State.INITIAL;

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        final Uri data = intent.getData();
        if (data != null) {
            parseTokenFromUri(data);
        }
        state = State.TOKEN_LOADED;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final YandexAuthOptions options = getIntent().getParcelableExtra(EXTRA_OPTIONS);

        loginHandler = new ExternalLoginHandler(options, () -> UUID.randomUUID().toString());

        if (savedInstanceState == null) {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setPackage(getIntent().getStringExtra(EXTRA_BROWSER_PACKAGE_NAME)); // FIXME Crash if flag "Don't keep activities" checked
            browserIntent.setData(Uri.parse(loginHandler.getUrl(options.getClientId())));
            startActivity(browserIntent);
            state = State.INITIAL;
        } else {
            loginHandler.restoreState(savedInstanceState);
            state = State.values()[savedInstanceState.getInt(STATE_LOADING_STATE)];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (state == State.TOKEN_REQUESTED) {
            finish();
        }
        state = State.TOKEN_REQUESTED;
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        loginHandler.saveState(outState);
        outState.putInt(STATE_LOADING_STATE, state.ordinal());
    }

    private void parseTokenFromUri(@NonNull final Uri data) {
        final Intent result = loginHandler.parseResult(data);
        setResult(RESULT_OK, result);
        finish();
    }
}
