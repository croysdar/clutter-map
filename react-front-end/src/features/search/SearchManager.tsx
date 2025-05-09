import React, { useState } from "react";
import { SearchToggle } from "./SearchToggle";
import { SearchModal } from "./SearchModal";

export const SearchManager: React.FC = () => {
    const [searchOpen, setSearchOpen] = useState(false);

    const handleOpenSearch = () => {
        // Blur any focused element (like the FAB) before opening the modal
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
        setSearchOpen(true);
    };

    return (
        <>
            <SearchToggle onOpenSearch={() => handleOpenSearch()} />
            <SearchModal open={searchOpen} onClose={() => setSearchOpen(false)} />
        </>
    );
};
