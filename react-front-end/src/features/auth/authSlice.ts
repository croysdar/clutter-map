import { client } from "@/services/client";
import type { AppDispatch, RootState } from "@/app/store";
import { createAppSlice } from "@/hooks/useAppHooks";
import { API_BASE_URL } from "@/utils/viteConstants";

interface UserInfo {
    userEmail: string | null,
    userName: string | null
    userFirstName: string | null
    userLastName: string | null
}

export interface AuthState extends UserInfo {
    status: AuthStatus
}

const initialState: AuthState = {
    userEmail: null,
    userName: null,
    userFirstName: null,
    userLastName: null,
    status: 'idle',
}

type AuthStatus = 'idle' | 'pending' | 'verified' | 'none'

type VerifyTokenReturn = { 'token': string }


const authSlice = createAppSlice({
    name: 'auth',
    initialState,
    reducers: create => {
        return {
            verifyToken: create.asyncThunk(
                async ({ idToken, provider }: { idToken: string, provider: string }) => {
                    const response = await client.post<VerifyTokenReturn>(`${API_BASE_URL}/auth/verify-token/${provider}`, idToken)

                    // Store the token in localStorage if the token exists
                    // This is done outside of the reducer to keep the reducer pure
                    if (response.data.token) {
                        localStorage.setItem('jwt', response.data.token);
                    }

                    return response.data
                },
                {
                    pending: (state) => {
                        state.status = 'pending'
                    },
                    fulfilled: (state, action) => {
                        state.status = 'verified'
                    },
                    rejected: (state) => {
                        state.status = 'none'
                    }
                }
            ),
            fetchUserInfo: create.asyncThunk(
                async (token: string, { rejectWithValue }) => {
                    const isOffline = !navigator.onLine;

                    let userInfo = null;
                    try {
                        const cachedUserInfo = localStorage.getItem('userInfo');
                        if (cachedUserInfo) {
                            userInfo = JSON.parse(cachedUserInfo);
                        }
                    } catch (error) {
                        console.error("Corrupted userInfo in localStorage. Clearing it.", error);
                        localStorage.removeItem('userInfo'); // Prevent further issues
                    }

                    const lastFetch = userInfo?.lastFetched;
                    const fiveMinutes = 5 * 60 * 1000; // 5 min threshold
                    const now = Date.now();

                    // Use cached data if it's recent (skip API call)
                    if (lastFetch && now - parseInt(lastFetch) < fiveMinutes) {
                        console.warn("Skipping fetchUserInfo: Already fetched recently");
                        return userInfo;
                    }

                    // If offline, use cached data
                    if (isOffline && userInfo) {
                        console.warn('Offline: Using cached user info');
                        return userInfo;
                    }

                    try {
                        const response = await client.get<UserInfo>(`${API_BASE_URL}/auth/user-info`, {
                            headers: { Authorization: `Bearer ${token}` }
                        });

                        const userInfoWithLastFetched = {
                            ...response.data,
                            lastFetched: Date.now() // Add the current timestamp for lastFetched
                        };

                        localStorage.setItem('userInfo', JSON.stringify(userInfoWithLastFetched));

                        return userInfoWithLastFetched;
                    }
                    catch (error: any) {
                        if (error.status) {
                            return rejectWithValue({
                                message: error.message,
                                status: error.status
                            })
                        }
                        throw error
                    }
                },
                {
                    pending: (state) => {
                        state.status = 'pending';
                    },
                    fulfilled: (state, action) => {
                        state.userEmail = action.payload.userEmail;
                        state.userName = action.payload.userName;
                        state.userFirstName = action.payload.userFirstName;
                        state.userLastName = action.payload.userLastName;
                        state.status = 'verified'
                    },
                    rejected: (state, action) => {
                        let errorPayload;
                        try {
                            errorPayload = action.error.message && JSON.parse(action.error.message);
                        }
                        catch (error: any) {
                            console.error("Error parsing the error message: ", error)
                        }

                        const statusCode = errorPayload?.status

                        if (!navigator.onLine) {
                            // If offline, keep the cached user info in state
                            console.warn('Fetch user info failed due to offline status');
                            return;
                        }

                        if (statusCode === 500) {
                            console.error('Server error: ', action.error)
                        }
                        else if (statusCode === 401 || statusCode === 403) {
                            // Invalid token
                            localStorage.removeItem('jwt');
                            localStorage.removeItem('userInfo');
                            state.userEmail = null;
                            state.userName = null;
                            state.userFirstName = null;
                            state.userLastName = null;
                            state.status = 'none'
                        }
                        else {
                            console.error('Failed to fetch user info: ', action.error)
                        }
                    }
                }
            ),
            rejectAuthStatus: create.reducer(
                (state) => {
                    state.userEmail = null;
                    state.userName = null;
                    state.userFirstName = null;
                    state.userLastName = null;
                    state.status = 'none'
                }
            ),
        }
    },
})


// Additional action to handle local storage cleanup when the user logs out
export const logoutUser = () => (dispatch: AppDispatch) => {
    // Clear local storage
    localStorage.removeItem('jwt');
    localStorage.removeItem('userInfo');

    // Update state
    dispatch(rejectAuthStatus());
};

export const { verifyToken, fetchUserInfo, rejectAuthStatus } = authSlice.actions

export const selectCurrentUserName = (state: RootState) => state.auth.userName
export const selectCurrentUserFirstName = (state: RootState) => state.auth.userFirstName
export const selectCurrentUserEmail = (state: RootState) => state.auth.userEmail
export const selectAuthStatus = (state: RootState) => state.auth.status

export default authSlice.reducer