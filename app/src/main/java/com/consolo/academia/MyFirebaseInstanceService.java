package com.consolo.academia;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceService extends FirebaseInstanceIdService {

    @Override
    public void onCreate() {
        String CurrentToken = FirebaseInstanceId.getInstance().getToken();

        String savedToken = Util.getFirebaseInstanceId(getApplicationContext());
        String defaultToken = getApplication().getString(R.string.pref_firebase_instance_id_default_key);

        if (CurrentToken != null && !savedToken.equalsIgnoreCase(defaultToken))
        //currentToken is null when app is first installed and token is not available
        //also skip if token is already saved in preferences...
        {
            Util.setFirebaseInstanceId(getApplicationContext(), CurrentToken);
        }
        super.onCreate();
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Util.setFirebaseInstanceId(getApplicationContext(), token);
    }

}
