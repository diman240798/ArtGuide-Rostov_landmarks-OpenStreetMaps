package com.swg_games_lab.nanicki.artguide.listener;

import android.support.annotation.NonNull;

import org.osmdroid.bonuspack.routing.Road;

public interface RouteReceiver {
    void onRouteReceived(@NonNull Road road);

    void onRouteBadReceived();
}
