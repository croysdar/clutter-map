import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import AppBreadcrumbs from '../Breadcrumbs';
import { ROUTES } from '@/utils/constants';

// Mock the API hooks
jest.mock('@/features/items/itemApi', () => ({
    useGetItemQuery: () => ({
        data: { id: 1, name: 'Test Item' },
        isLoading: false
    })
}));

jest.mock('@/features/orgUnits/orgUnitApi', () => ({
    useGetOrgUnitQuery: () => ({
        data: { id: 1, name: 'Test Org Unit' },
        isLoading: false
    })
}));

jest.mock('@/features/projects/projectApi', () => ({
    useGetProjectQuery: () => ({
        data: { id: 1, name: 'Test Project' },
        isLoading: false
    })
}));

jest.mock('@/features/rooms/roomApi', () => ({
    useGetRoomQuery: () => ({
        data: { id: 1, name: 'Test Room' },
        isLoading: false
    })
}));

// Mock the entity hierarchy hook
jest.mock('@/hooks/useEntityHierarchy', () => ({
    useEntityHierarchy: () => ({
        hierarchy: {
            project: { id: 1, name: 'Test Project' },
            room: { id: 1, name: 'Test Room' },
            orgUnit: { id: 1, name: 'Test Org Unit' }
        },
        loading: false
    })
}));

// Mock the resolved params hook
jest.mock('@/hooks/useResolvedParams', () => ({
    useResolvedParams: () => ({
        projectId: '1',
        roomId: '1',
        orgUnitId: '1',
        itemId: '1',
        projectIdNum: 1,
        roomIdNum: 1,
        orgUnitIdNum: 1,
        itemIdNum: 1
    })
}));

const renderWithRouter = (initialRoute: string) => {
    const store = configureStore({
        reducer: {
            dummy: () => ({})
        }
    });

    return render(
        <Provider store={store}>
            <MemoryRouter initialEntries={[initialRoute]}>
                <Routes>
                    <Route path="*" element={<AppBreadcrumbs />} />
                </Routes>
            </MemoryRouter>
        </Provider>
    );
};

describe('Breadcrumbs', () => {
    it('renders home breadcrumb on home page', () => {
        renderWithRouter(ROUTES.home);
        expect(screen.getByText('Home')).toBeInTheDocument();
    });

    it('renders about breadcrumb on about page', () => {
        renderWithRouter(ROUTES.about);
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('About')).toBeInTheDocument();
    });

    it('renders projects breadcrumb on projects page', () => {
        renderWithRouter(ROUTES.projects);
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
    });

    it('renders project breadcrumb on project details page', () => {
        renderWithRouter(ROUTES.projectDetails('1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
    });

    it('renders add breadcrumb on project add page', () => {
        renderWithRouter(ROUTES.projectAdd);
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Add')).toBeInTheDocument();
    });

    it('renders room breadcrumb on room details page', () => {
        renderWithRouter(ROUTES.roomDetails('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Room')).toBeInTheDocument();
    });

    it('renders add breadcrumb on room add page', () => {
        renderWithRouter(ROUTES.roomAdd('1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Add')).toBeInTheDocument();
    });

    it('renders org unit breadcrumb on org unit details page', () => {
        renderWithRouter(ROUTES.orgUnitDetails('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Org Unit')).toBeInTheDocument();
    });

    it('renders add breadcrumb on org unit add page', () => {
        renderWithRouter(ROUTES.orgUnitAdd('1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Add')).toBeInTheDocument();
    });

    it('renders item breadcrumb on item details page', () => {
        renderWithRouter(ROUTES.itemDetails('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Item')).toBeInTheDocument();
    });

    it('renders add breadcrumb on item add page', () => {
        renderWithRouter(ROUTES.itemAdd('1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Add')).toBeInTheDocument();
    });

    it('renders edit breadcrumb on item edit page', () => {
        renderWithRouter(ROUTES.itemEdit('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Item')).toBeInTheDocument();
        expect(screen.getByText('Edit')).toBeInTheDocument();
    });

    it('renders edit breadcrumb on org unit edit page', () => {
        renderWithRouter(ROUTES.orgUnitEdit('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Org Unit')).toBeInTheDocument();
        expect(screen.getByText('Edit')).toBeInTheDocument();
    });

    it('renders edit breadcrumb on room edit page', () => {
        renderWithRouter(ROUTES.roomEdit('1', '1'));
        expect(screen.getByText('Home')).toBeInTheDocument();
        expect(screen.getByText('My Projects')).toBeInTheDocument();
        expect(screen.getByText('Test Project')).toBeInTheDocument();
        expect(screen.getByText('Test Room')).toBeInTheDocument();
        expect(screen.getByText('Edit')).toBeInTheDocument();
    });
}); 