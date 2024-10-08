import { client } from "@/api/client";
import { RootState } from "@/app/store";
import { createAppSlice } from "@/hooks/useAppHooks";
import { API_BASE_URL } from "@/utils/constants";

export interface AuthState {
    token: string | null,
    userEmail: string | null,
    userName: string | null,
    status: AuthStatus
}

const initialState: AuthState = {
    token: null,
    userEmail: null,
    userName: null,
    status: 'idle',
}

type AuthStatus = 'idle' | 'pending' | 'verified' | 'none'

interface UserInfoReturn {
    userEmail: string,
    userName: string
}

type VerifyTokenReturn = Pick<AuthState, 'token' | 'userEmail' | 'userName'>


const authSlice = createAppSlice({
    name: 'auth',
    initialState,
    reducers: create => {
        return {
            verifyToken: create.asyncThunk(
                // TODO figure out how to implement csrf token
                async (idToken: string) => {
                    const response = await client.post<VerifyTokenReturn>(`${API_BASE_URL}/auth/verify-token`, idToken)

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
                        state.token = action.payload.token
                        state.userEmail = action.payload.userEmail
                        state.userName = action.payload.userName
                        state.status = 'verified'
                    },
                    rejected: (state) => {
                        state.status = 'none'
                    }
                }
            ),
            fetchUserInfo: create.asyncThunk(
                async (token: string, {rejectWithValue}) => {
                    try {
                        const response = await client.get<UserInfoReturn>(`${API_BASE_URL}/auth/user-info`, {
                            headers: { Authorization: `Bearer ${token}` }
                        });

                        return response.data
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
                        state.status = 'verified'
                    },
                    rejected: (state, action) => {
                        let errorPayload;
                        try {
                            errorPayload = action.error.message && JSON.parse(action.error.message);
                        }
                        catch (error: any) {
                            console.log("Error parsing the error message: ", error)
                        }

                        const statusCode = errorPayload?.status

                        if (statusCode === 500) {
                            console.error('Server error: ', action.error)
                        }
                        else if (statusCode === 401 || statusCode === 403) {
                            // Invalid token
                            localStorage.removeItem('jwt')
                            state.token = null;
                            state.userEmail = null;
                            state.userName = null;
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
                    state.status = 'none';
                }
            ),
        }
    },
})

export const { verifyToken, fetchUserInfo, rejectAuthStatus } = authSlice.actions

export const selectCurrentUserName = (state: RootState) => state.auth.userName
export const selectCurrentUserEmail = (state: RootState) => state.auth.userEmail
export const selectAuthStatus = (state: RootState) => state.auth.status

export default authSlice.reducer