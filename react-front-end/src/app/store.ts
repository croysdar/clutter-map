import { Action, configureStore, ThunkAction } from "@reduxjs/toolkit";

import { apiSlice } from "@/features/api/apiSlice";

export const store = configureStore({
    reducer: {
        [apiSlice.reducerPath]: apiSlice.reducer
    },
    middleware: getDefaultMiddleware => 
        getDefaultMiddleware().concat(apiSlice.middleware)
})


// Infer the type of `store`
export type AppStore = typeof store
export type RootState = ReturnType<AppStore['getState']>

// Infer the `AppDispatch` type from the store itself
export type AppDispatch = AppStore['dispatch']

// Define a reusable type describing thunk functions
export type AppThunk = ThunkAction<void, RootState, unknown, Action>