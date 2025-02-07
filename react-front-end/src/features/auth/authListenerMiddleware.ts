import { createListenerMiddleware, isAnyOf } from "@reduxjs/toolkit";
import { fetchUserInfo, verifyToken } from "@/features/auth/authSlice";
import { syncIDB } from "@/features/offline/syncSlice";

const authListenerMiddleware = createListenerMiddleware();

authListenerMiddleware.startListening({
    matcher: isAnyOf(fetchUserInfo.fulfilled),
    effect: async (_, listenerApi) => {
        console.log("fetchUserInfo was successful. Triggering sync...");
        listenerApi.dispatch(syncIDB());
    }
});

authListenerMiddleware.startListening({
    matcher: isAnyOf(verifyToken.fulfilled),
    effect: async (_, listenerApi) => {
        const jwt = localStorage.getItem('jwt');
        if (jwt) {
            console.log("Verify token was successful. Triggering user info fetch...");
            listenerApi.dispatch(fetchUserInfo(jwt));
        }
    }
});

export default authListenerMiddleware;
