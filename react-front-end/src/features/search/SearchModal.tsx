import React, { useState } from "react";
import {
    Dialog,
    DialogTitle,
    DialogContent,
    TextField,
    IconButton,
    Button,
    List,
    ListItem,
    ListItemText,
    DialogActions,
    Box,
    Pagination,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import { useGetItemsByProjectQuery } from "../items/itemApi";
import { useNavigate, useParams } from "react-router-dom";
import { searchItems } from "./search";
import { Item } from "../items/itemTypes";
import { ROUTES } from "@/utils/constants";

interface SearchModalProps {
    open: boolean;
    onClose: () => void;
}

export const SearchModal: React.FC<SearchModalProps> = ({ open, onClose }) => {
    const [query, setQuery] = useState("");
    const [results, setResults] = useState<Item[]>([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);
    const itemsPerPage = 10;

    const navigate = useNavigate();

    const { projectId } = useParams();
    const { data: allItems, isLoading: itemsLoading } = useGetItemsByProjectQuery(Number(projectId)!);

    const handleSearch = async () => {
        if (itemsLoading || !allItems) return

        setLoading(true);
        const filtered = searchItems(allItems, query);
        setResults(filtered);
        setLoading(false);
    };

    return (
        <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
            <DialogTitle>
                Search Items
                <IconButton
                    aria-label="close"
                    onClick={onClose}
                    sx={{ position: "absolute", right: 8, top: 8 }}
                >
                    <CloseIcon />
                </IconButton>
            </DialogTitle>

            <DialogContent dividers>
                <TextField
                    fullWidth
                    label="Search by name, description, or tag"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    onKeyDown={(e) => {
                        if (e.key === "Enter") {
                            handleSearch();
                        }
                    }}
                />

                <Button
                    onClick={handleSearch}
                    variant="contained"
                    sx={{ mt: 2 }}
                    disabled={loading}
                >
                    {loading ? "Searching..." : "Search"}
                </Button>

                <List sx={{ maxHeight: 300, overflow: "auto", mt: 2 }}>
                    {results.slice((page - 1) * itemsPerPage, page * itemsPerPage).map((item) => {
                        let route = "";
                        if (item.orgUnitId) {
                            route = ROUTES.itemDetails(Number(projectId)!, item.id);
                        }
                        return (
                            <ListItem key={item.id} onClick={() => {
                                navigate(route);
                                onClose();
                            }}
                                sx={{
                                    cursor: "pointer",
                                    borderRadius: 1,
                                    transition: 'background-color 0.2s ease-in-out',
                                    "&:hover": {
                                        backgroundColor: "primary.main",
                                        color: "primary.contrastText",
                                        "& .MuiTypography-root": {
                                            color: "primary.contrastText",
                                        }
                                    }
                                }}
                            >
                                <ListItemText
                                    primary={item.name}
                                    secondary={`${item.description} • Tags: ${item.tags.join(", ")}`}
                                />
                            </ListItem>
                        )
                    })}
                    <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}>
                        <Pagination
                            count={Math.ceil(results.length / itemsPerPage)}
                            page={page}
                            onChange={(_, value) => setPage(value)}
                            color="primary"
                        />
                    </Box>
                </List>
            </DialogContent>

            <DialogActions>
                <Button onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    );
};
