import { ResourceType, TimelineActionType } from "@/types/types";

export interface Event {
    action: TimelineActionType
    details: string
    entityId: number
    entityType: ResourceType
    timestamp: Date
    userId: number
    userName: string
}