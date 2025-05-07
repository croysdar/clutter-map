import React, { useState } from "react";
import { SearchToggle } from "./SearchToggle";
import { SearchModal } from "./SearchModal";

export const SearchManager: React.FC = () => {
    const [searchOpen, setSearchOpen] = useState(false);

    return (
        <>
            <SearchToggle onOpenSearch={() => setSearchOpen(true)} />
            <SearchModal open={searchOpen} onClose={() => setSearchOpen(false)} />
        </>
    );
};
