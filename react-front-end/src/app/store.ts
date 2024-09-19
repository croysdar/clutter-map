import { Action, configureStore, ThunkAction } from "@reduxjs/toolkit";

import { rootReducer } from "./rootReducer";

export const store = configureStore({
    reducer: rootReducer
})


// Infer the type of `store`
export type AppStore = typeof store
export type RootState = ReturnType<AppStore['getState']>

// Infer the `AppDispatch` type from the store itself
export type AppDispatch = AppStore['dispatch']

// Define a reusable type describing thunk functions
export type AppThunk = ThunkAction<void, RootState, unknown, Action>