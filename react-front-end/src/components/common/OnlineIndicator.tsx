import { Wifi, WifiOff } from "@mui/icons-material";
import { useEffect, useState } from "react";

const OnlineIndicator: React.FC = () => {

    const [isOnline, setIsOnline] = useState(navigator.onLine);

    useEffect(() => {
        const updateOnlineStatus = () => setIsOnline(!!navigator.onLine);

        window.addEventListener('online', updateOnlineStatus);
        window.addEventListener('offline', updateOnlineStatus);

        return () => {
            window.removeEventListener('online', updateOnlineStatus);
            window.removeEventListener('offline', updateOnlineStatus);
        };
    }, []);

    return (<>
        {
            isOnline ?
                <Wifi />
                :
                <WifiOff color="error" />
        }
    </>)
}

export default OnlineIndicator;