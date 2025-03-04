import { ResourceType, TimelineActionType } from "@/types/types";

export interface TimelineEvent {
    id: number,
    action: TimelineActionType,
    entityId: number,
    entityType: ResourceType,
    projectId: number,
    projectName: string,
    userId: number,
    userName: string,
    details: string,
    timestamp: string
}