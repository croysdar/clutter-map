import { ReactElement, useEffect, useState } from 'react';
import { Navigate, useSearchParams } from 'react-router-dom';

import { useResolvedParams } from '@/hooks/useResolvedParams';

type RequiresOnlineProps = {
    children: ReactElement;
    redirectUrl: string | ((...args: any[]) => string);
    alternateRedirectUrl?: ((...args: any[]) => string);
    alternateRedirectParamKey?: string;
};

const RequiresOnline = ({
    children,
    redirectUrl,
    alternateRedirectUrl,
    alternateRedirectParamKey
}: RequiresOnlineProps) => {
    const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);
    const [searchParams] = useSearchParams();
    const { projectId, roomId, orgUnitId, itemId } = useResolvedParams();

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

        let resolvedRedirectUrl: string;

        const altParamValue = alternateRedirectParamKey
            ? searchParams.get(alternateRedirectParamKey)
            : null;

        if (altParamValue && alternateRedirectUrl) {
            let resolvedOrgUnitId = orgUnitId;
            let resolvedRoomId = roomId;
            if (alternateRedirectParamKey === "orgUnitId")
                resolvedOrgUnitId = altParamValue

            if (alternateRedirectParamKey === "roomId")
                resolvedRoomId = altParamValue

            resolvedRedirectUrl = alternateRedirectUrl(projectId, resolvedRoomId, resolvedOrgUnitId, itemId)
                .replace(/\/undefined/g, '')  // Remove undefined segments
                .replace(/\/$/, '');         // Remove trailing slash if needed
        } else if (typeof redirectUrl === 'function') {
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
