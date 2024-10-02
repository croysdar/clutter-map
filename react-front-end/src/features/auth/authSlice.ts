import { client } from "@/api/client";
import { createAppSlice } from "@/hooks/useAppHooks";

export interface AuthState {
    token: string,
    userEmail: String,
    userName: String,
    // status: String
}

const initialState: AuthState = {
    token: '',
    userEmail: '',
    userName: '',
    // status: '',
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
            )
        }
    },
})

export const { login } = authSlice.actions

export default authSlice.reducer