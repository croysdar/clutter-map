import { useDispatch, useSelector } from 'react-redux'
import type { AppDispatch, RootState } from '@app/store'
import { asyncThunkCreator, buildCreateSlice } from '@reduxjs/toolkit'

// Use throughout app instead of plain `useDispatch` and `useSelector`
export const useAppDispatch = useDispatch.withTypes<AppDispatch>()
export const useAppSelector = useSelector.withTypes<RootState>()

export const createAppSlice = buildCreateSlice({ creators: {asyncThunk: asyncThunkCreator}})