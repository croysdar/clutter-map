import { useLocation, useSearchParams, matchPath } from "react-router-dom";
import { ROUTE_PATTERNS } from "@/utils/constants";

export const useExtractParams = () => {
    const { pathname } = useLocation();

    for (const { pattern } of ROUTE_PATTERNS) {
        const match = matchPath(pattern, pathname);
        if (match?.params) {
            return match.params as Record<string, string | undefined>;
        }
    }

    return {};
};

export const useResolvedParams = () => {
    const params = useExtractParams();
    const [searchParams] = useSearchParams();

    const get = (key: string): string | undefined =>
        params[key] ?? searchParams.get(key) ?? undefined;

    const getNum = (key: string): number | undefined => {
        const val = get(key);
        return val !== undefined && !isNaN(Number(val)) ? Number(val) : undefined;
    };

    return {
        projectId: get("projectId"),
        roomId: get("roomId"),
        orgUnitId: get("orgUnitId"),
        itemId: get("itemId"),
        projectIdNum: getNum("projectId"),
        roomIdNum: getNum("roomId"),
        orgUnitIdNum: getNum("orgUnitId"),
        itemIdNum: getNum("itemId"),
    };
};
