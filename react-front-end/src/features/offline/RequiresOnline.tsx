import { ReactElement, useEffect, useState } from 'react';
import { Navigate, redirect, useLocation, useParams } from 'react-router-dom';


type RequiresOnlineProps = {
    children: ReactElement;
    redirectUrl: string | ((...args: any[]) => string);
};

const RequiresOnline = ({ children, redirectUrl }: RequiresOnlineProps) => {
    const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);
    const { projectId, roomId, orgUnitId, itemId } = useParams();

    useEffect(() => {
        const handleOnline = () => setIsOnline(true);
        const handleOffline = () => setIsOnline(false);

        window.addEventListener('online', handleOnline);
        window.addEventListener('offline', handleOffline);

        return () => {
            window.removeEventListener('online', handleOnline);
            window.removeEventListener('offline', handleOffline);
        };
    }, []);

    if (!isOnline) {
        let resolvedRedirectUrl;
        if (typeof redirectUrl === 'function') {
            resolvedRedirectUrl = redirectUrl(projectId, roomId, orgUnitId, itemId)
                .replace(/\/undefined/g, '')  // Remove undefined segments
                .replace(/\/$/, '');         // Remove trailing slash if needed
        }
        else {
            resolvedRedirectUrl = redirectUrl;
        }
        return <Navigate to={resolvedRedirectUrl} replace />;
    }

    return children;
};

export default RequiresOnline;
