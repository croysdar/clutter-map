import { Item } from "@/features/items/itemTypes";

export function searchItems(items: Item[], query: string): Item[] {
    const lowerQuery = query.trim().toLowerCase();

    return items.filter(item => {
        const nameMatch = item.name.toLowerCase().includes(lowerQuery);
        const descriptionMatch = item.description.toLowerCase().includes(lowerQuery);
        const tagMatch = item.tags.some(tag => tag.toLowerCase().includes(lowerQuery));

        return nameMatch || descriptionMatch || tagMatch;
    });
}
