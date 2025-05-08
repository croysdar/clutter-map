import React, { useState, useEffect, useRef, forwardRef, Ref } from "react";
import { Fab, Tooltip, Menu, MenuItem, Paper, Typography } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";

export const SearchToggle: React.FC<{
    onOpenSearch: () => void;
}> = ({ onOpenSearch }) => {
    const [menuAnchorEl, setMenuAnchorEl] = useState<null | HTMLElement>(null);
    const [showSearchFab, setShowSearchFab] = useState(true);

    useEffect(() => {
        const stored = localStorage.getItem("showSearchFab");
        if (stored === null) {
            showFab()
        }
        else if (stored === "false") {
            setShowSearchFab(false);
        }
    }, []);

    const hideFab = () => {
        console.log('hiding fab')
        setShowSearchFab(false);
        localStorage.setItem("showSearchFab", "false");
        setMenuAnchorEl(null);
    };

    const showFab = () => {
        setShowSearchFab(true);
        localStorage.setItem("showSearchFab", "true");
    };

    return (
        <>
            {showSearchFab ? (
                <>
                    <Tooltip title="Search">
                        <SwipeToDismissFab
                            onDismiss={hideFab}
                            onClick={onOpenSearch}
                            onRightClick={(e) => {
                                e.preventDefault();
                                setMenuAnchorEl(e.currentTarget);
                            }}
                        />
                    </Tooltip>

                    <Menu
                        anchorEl={menuAnchorEl}
                        open={Boolean(menuAnchorEl)}
                        onClose={() => setMenuAnchorEl(null)}
                    >
                        <MenuItem onClick={hideFab}>Hide Search Button</MenuItem>
                    </Menu>
                </>
            ) : (
                <Paper
                    elevation={2}
                    sx={{
                        position: 'fixed',
                        top: '85%',
                        right: 0,
                        transform: 'translateY(-50%)',
                        backgroundColor: (theme) => theme.palette.background.paper,
                        color: (theme) => theme.palette.text.secondary,
                        padding: (theme) => theme.spacing(1, 0.5),
                        borderTopLeftRadius: (theme) => theme.shape.borderRadius,
                        borderBottomLeftRadius: (theme) => theme.shape.borderRadius,
                        borderTopRightRadius: 0,
                        borderBottomRightRadius: 0,
                        cursor: 'pointer',
                        writingMode: 'vertical-rl',
                        textAlign: 'center',
                        zIndex: (theme) => theme.zIndex.fab - 1,
                        transition: (theme) => theme.transitions.create(['transform', 'box-shadow']),
                        '&:hover': {
                            transform: 'translateY(-50%) translateX(-2px)',
                            color: (theme) => theme.palette.text.primary
                        },
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        opacity: 0.8,
                        '@media (max-width: 600px)': {
                            padding: (theme) => theme.spacing(0.75, 0.25),
                            opacity: 0.6
                        }
                    }}
                    onClick={showFab}
                >
                    <SearchIcon sx={{ fontSize: '1.2rem' }} />
                    <Typography variant="caption">Search</Typography>
                </Paper>
            )
            }
        </>
    )
}

interface SwipeToDismissFabProps {
    onDismiss: () => void;
    onClick: (e: React.MouseEvent) => void;
    onRightClick: (e: React.MouseEvent<HTMLElement>) => void;
}

export const SwipeToDismissFab = forwardRef(
    (
        { onDismiss, onClick, onRightClick, ...rest }: SwipeToDismissFabProps,
        ref: Ref<HTMLButtonElement>
    ) => {

        const [dragX, setDragX] = useState(0);
        const startX = useRef<number | null>(null);

        const dragThreshold = useRef(getDragThreshold());
        const suppressClick = useRef(false);

        useEffect(() => {
            const handleResize = () => {
                dragThreshold.current = getDragThreshold();
            };
            window.addEventListener("resize", handleResize);
            return () => window.removeEventListener("resize", handleResize);
        }, []);

        function getDragThreshold() {
            const width = window.innerWidth;
            if (width < 480) return 40;  // small mobile
            if (width < 768) return 60;  // large mobile/tablet
            return 40;                  // desktop
        }

        const handlePointerDown = (e: React.PointerEvent) => {
            console.log('pointer down')
            startX.current = e.clientX;
            e.currentTarget.setPointerCapture(e.pointerId);
        };

        const handlePointerMove = (e: React.PointerEvent) => {
            console.log('pointer move')
            if (startX.current === null) return

            const deltaX = e.clientX - startX.current;

            if (deltaX > 0) {
                setDragX(deltaX)
                if (deltaX > 5) {
                    suppressClick.current = true; // treat as swipe, not click
                    console.log('suppress')
                }
                if (deltaX > dragThreshold.current) onDismiss();
            }
            else {

                if (deltaX < (-1 * dragThreshold.current)) {
                    setDragX(0)
                    startX.current = null;
                }
                if (deltaX < -5) {
                    suppressClick.current = true; // treat as swipe, not click
                    console.log('suppress')
                }
            }
        };

        const handlePointerUp = (e: React.PointerEvent) => {
            console.log('pointer up')

            if (dragX > dragThreshold.current) {
                onDismiss();
            } else {
                setDragX(0);
            }

            startX.current = null;
            e.currentTarget.releasePointerCapture(e.pointerId);
        };

        const handleClick = (e: React.MouseEvent) => {
            if (suppressClick.current) {
                e.preventDefault();
                e.stopPropagation(); // suppress unintended click
                suppressClick.current = false;
                return;
            }
            onClick(e);
        };

        return (
            <Fab
                ref={ref}
                {...rest}

                color="primary"
                onClick={handleClick}

                onPointerDown={handlePointerDown}
                onPointerMove={handlePointerMove}
                onPointerUp={handlePointerUp}
                onPointerCancel={handlePointerUp}

                onContextMenu={onRightClick}
                style={{
                    transform: `translateX(${dragX}px)`,
                    transition: startX.current === null ? 'transform 0.3s ease' : 'none',
                    touchAction: 'none',
                    position: "fixed",
                    bottom: 16,
                    right: 16,
                    zIndex: 999,
                }}
            >
                <SearchIcon />
            </Fab>
        )
    });