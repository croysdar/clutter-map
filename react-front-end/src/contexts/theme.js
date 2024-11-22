import { appColors } from '@/assets/colors';
import { createTheme, responsiveFontSizes } from '@mui/material/styles';

let theme = createTheme({
    palette: {
        primary: {
            main: appColors.primary,
            light: appColors.primaryLight,
            dark: appColors.primaryDark,
            contrastText: '#fff',
        },
        secondary: {
            main: appColors.secondary,    // Main secondary color
            light: appColors.secondaryLight,   // Lighter shade
            dark: appColors.secondaryDark,    // Darker shade
            contrastText: '#000', // Text color on secondary
        },
        background: {
            default: appColors.backgroundDefault, // Default background color
            paper: appColors.backgroundPaper,   // Background color for paper surfaces
        },
        text: {
            primary: appColors.textPrimary,  // Primary text color
            secondary: appColors.textSecondary, // Secondary text color
        },
    },
    typography: {
        fontFamily: "'Roboto', 'Helvetica', 'Arial', sans-serif",
        h1: {
            fontSize: '2.5rem',
            fontWeight: 700,
            lineHeight: 1.2,
            // color: appColors.textPrimary
        },
        h2: {
            fontSize: '2rem',
            fontWeight: 700,
            lineHeight: 1.3,
            // color: appColors.textPrimary
        },
        // h3: {
        // fontSize: '2rem',
        // fontWeight: 700,
        // lineHeight: 1.3,
        // color: appColors.textPrimary
        // },
        // h4: {
        //     color: appColors.textPrimary
        // },
        // h5: {
        //     color: appColors.textPrimary
        // },
        // h6: {
        //     color: appColors.textPrimary
        // },
        // subtitle1: {
        //     color: appColors.textPrimary
        // },
        // subtitle2: {
        //     color: appColors.textPrimary
        // },
        body1: {
            fontSize: '1rem',
            fontWeight: 400,
            // color: appColors.textPrimary
        },
        body2: {
            // color: appColors.textPrimary
        },
        button: {
            fontWeight: 600,
            textTransform: 'uppercase',
        },
    },
    spacing: 8, // Base spacing, useful for padding, margins, etc.

    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 8,  // Rounded corners for buttons
                    padding: '8px 16px',
                },
                contained: {
                    boxShadow: '0px 4px 6px rgba(0, 0, 0, 0.1), 0px 1px 3px rgba(0, 0, 0, 0.08)',
                    fontWeight: 'bold',
                    '&:hover': {
                        boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)', // Slight shadow on hover
                    },
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                colorPrimary: {
                    backgroundColor: '#1976d2',
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: '12px',
                    boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)',
                    padding: '16px',
                    marginBottom: '16px',
                    maxWidth: '600px',
                    margin: '0 auto',
                },
            },
        },
        MuiCardContent: {
            styleOverrides: {
                root: {
                    '&:last-child': {
                        paddingBottom: 0,
                    },
                },
            },
        },
        // MuiTypography: {
        //     variants: [
        //         {
        //             props: { variant: 'subtitle2' },
        //             style: {
        //                 fontWeight: 'bold',
        //                 color: '#757575',
        //             },
        //         },
        //         {
        //             props: { variant: 'h6' },
        //             style: {
        //                 fontWeight: 'bold',
        //                 fontSize: '1.25rem',
        //             },
        //         },
        //     ],
        // },
    },
});

theme = responsiveFontSizes(theme);

export default theme;
