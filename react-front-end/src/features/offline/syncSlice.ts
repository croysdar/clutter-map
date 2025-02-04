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

let syncAbortController: AbortController | null = null;
let initAbortController: AbortController | null = null;

const syncSlice = createAppSlice({
    name: 'sync',
    initialState,
    reducers:
        create => {
            return {
                syncIDB: create.asyncThunk(
                    async (token: string, { rejectWithValue }) => {
                        if (syncAbortController) {
                            console.warn("Sync already in progress. Cancelling duplicate sync.");
                            return;
                        }

                        syncAbortController = new AbortController();

                        try {
                            await syncFunction(token);
                            return { success: true }
                        }
                        catch (error: any) {
                            return rejectWithValue(error.message || 'Sync failed');
                        } finally {
                            syncAbortController = null;
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
                        if (initAbortController) {
                            console.warn("Initialization already in progress. Cancelling duplicate init.");
                            return;
                        }

                        initAbortController = new AbortController();

                        try {
                            await initFunction();
                            return { success: true }
                        }
                        catch (error: any) {
                            return rejectWithValue(error.message || 'Initialization failed');
                        } finally {
                            initAbortController = null;
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
