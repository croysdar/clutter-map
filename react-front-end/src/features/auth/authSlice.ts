import { client } from "@/api/client";
import { RootState } from "@/app/store";
import { createAppSlice } from "@/hooks/useAppHooks";

export interface AuthState {
    token: string | null,
    userEmail: string | null,
    userName: string | null,
    // status: string
}

const initialState: AuthState = {
    token: null,
    userEmail: null,
    userName: null,
    // status: '',
}

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
            login: create.asyncThunk(
                // TODO figure out how to implement csrf token
                async (idToken: string) => {
                    const response = await client.post<VerifyTokenReturn>('http://localhost:8080/auth/verify-token', idToken)

                    // Store the token in localStorage if the token exists
                    // This is done outside of the reducer to keep the reducer pure
                    if (response.data.token) {
                        localStorage.setItem('jwt', response.data.token);
                    }

                    return response.data
                },
                {
                    fulfilled: (state, action) => {
                        state.token = action.payload.token
                        state.userEmail = action.payload.userEmail
                        state.userName = action.payload.userName
                    }
                }
            ),
            fetchUserInfo: create.asyncThunk(
                async (token: string, {rejectWithValue}) => {
                    try {
                        const response = await client.get<UserInfoReturn>('http://localhost:8080/auth/user-info', {
                            headers: { Authorization: `Bearer ${token}` }
                        });

                        return response.data
                    }
                    catch (error: any) {
                        if (error.response) {
                            return rejectWithValue({
                                message: error.message,
                                status: error.response.status
                            })
                        }
                        throw error
                    }

                },
                {
                    fulfilled: (state, action) => {
                        state.userEmail = action.payload.userEmail;
                        state.userName = action.payload.userName;
                    },
                    rejected: (state, action) => {
                        const errorPayload = action.payload as { status?: number };
                        const statusCode = errorPayload?.status

                        if (statusCode === 500) {
                            console.error('Server error: ', action.error)
                        }
                        else if (statusCode === 401 || statusCode === 403) {
                            console.error('Unauthorized:', action.payload);

                            // Invalid token
                            localStorage.removeItem('jwt')
                            state.token = null;
                            state.userEmail = null;
                            state.userName = null;
                        } 
                        else {
                            console.error('Failed to fetch user info: ', action.error)
                        }
                    }
                }
            )
        }
    },
})

export const { login, fetchUserInfo } = authSlice.actions

export const selectCurrentUserName = (state: RootState) => state.auth.userName
export const selectCurrentUserEmail = (state: RootState) => state.auth.userEmail
export const selectCurrentAuthToken = (state: RootState) => state.auth.token

export default authSlice.reducer