import { createAppSlice } from "@/hooks/useAppHooks";

export interface AuthState {

}

const initialState: AuthState = {

}

const authSlice = createAppSlice({
    name: 'auth',
    initialState,
    reducers: {

    },
})

export default authSlice.reducer