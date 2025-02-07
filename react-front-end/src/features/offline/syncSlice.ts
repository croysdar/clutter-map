/* ------------- Redux ------------- */
import { createAppSlice } from '@/hooks/useAppHooks';

/* ------------- Indexed DB ------------- */
import { initDB as initFunction, performSync as syncFunction } from './idb';

export interface SyncState {
    status: string
}

const initialState: SyncState = {
    status: 'idle'
}

const syncSlice = createAppSlice({
    name: 'sync',
    initialState,
    reducers:
        create => {
            return {
                syncIDB: create.asyncThunk(
                    async (_, { rejectWithValue }) => {
                        const token = localStorage.getItem('jwt');
                        if (!token)
                            return;
                        try {
                            await syncFunction(token);
                            return { success: true }
                        }
                        catch (error: any) {
                            return rejectWithValue(error.message || 'Sync failed');
                        }
                    },
                    {
                        pending: (state) => {
                            state.status = 'syncing';
                        },
                        fulfilled: (state, action) => {
                            state.status = 'success';
                        },
                        rejected: (state) => {
                            state.status = 'failed';
                        }
                    }
                ),
                initIDB: create.asyncThunk(
                    async (_, { rejectWithValue }) => {
                        try {
                            await initFunction();
                            return { success: true }
                        }
                        catch (error: any) {
                            return rejectWithValue(error.message || 'Initialization failed');
                        }
                    },
                    {
                        pending: (state) => {
                            state.status = 'initializing';
                        },
                        fulfilled: (state, action) => {
                            state.status = 'success';
                        },
                        rejected: (state) => {
                            state.status = 'failed';
                        }
                    }
                )
            }
        }
});

export const { syncIDB, initIDB } = syncSlice.actions;
export default syncSlice.reducer;
