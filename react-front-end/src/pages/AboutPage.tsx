import { Box, Container, List, ListItem, ListItemText, Typography } from '@mui/material';
import React from 'react';

const AboutPage: React.FC = () => {
    return (
        <Container maxWidth="md">
            <Typography variant='h3'>
                About Clutter Map
            </Typography>

            <Typography variant='h4' gutterBottom sx={{ mt: 3 }}>
                What is Clutter Map?
            </Typography>
            <Typography variant='body1' paragraph>
                Clutter Map is an innovative web application designed to help users
                keep track of their belongings. It doesn't enforce strict organization
                but instead provides a system for knowing exactly where everything belongs,
                making it easier to put items away or find them later. Clutter Map has a
                wide range of potential uses, from personal organization to enhancing the
                guest experience in short-term rentals by ensuring everything is in its
                designated place.
            </Typography>

            <Typography variant='h4' gutterBottom sx={{ mt: 6 }}>
                Why I created Clutter Map
            </Typography>
            <Typography variant='body1' paragraph>
                I built Clutter Map to address a common issue: people often don’t know where
                items belong, making it challenging to put things away or keep spaces
                organized. This app was inspired by several real-life scenarios:
            </Typography>
            <List>
                <ListItem>
                    <ListItemText
                        primary='“Where does this go?”'
                        secondary="When asked to help clean, people often respond, 
                    “I don't know where those things go.” Clutter Map eliminates this barrier by 
                    clearly showing the designated spot for each item."
                    />
                </ListItem>
                <ListItem>
                    <ListItemText
                        primary="Rental Properties"
                        secondary="A friend with a beach house rental expressed frustration when 
                        guests and cleaners fail to return items to their proper places. Clutter 
                        Map provides a solution by helping everyone know where things belong, 
                        reducing the hassle of reorganization after each rental."
                    />
                </ListItem>
                <ListItem>
                    <ListItemText
                        primary="Community Events"
                        secondary="Another friend runs a community board game event and hopes to 
                        make it easy for guests to return games to their designated spots after use. 
                        Clutter Map could provide visual guides to ensure everything ends up where it 
                        started."
                    />
                </ListItem>
                <ListItem>
                    <ListItemText
                        primary="Camping Trips"
                        secondary="On a recent camping trip, we used a shared to-do list app to track 
                        where items were stored, allowing everyone to locate items and help pack up. 
                        However, as people checked items off, they disappeared from the list, forcing us 
                        to re-add everything later. Clutter Map offers a more efficient way to track 
                        items without losing the list as they’re marked as stored."
                    />
                </ListItem>
            </List>
            <Typography variant="body1" paragraph>
                In addition to solving these everyday problems, Clutter Map has been a valuable project
                for me to deepen my skills, explore new technologies, and build a genuinely useful application.
            </Typography>


            <Typography variant='h4' gutterBottom sx={{ mt: 6 }}>
                Technologies
            </Typography>
            <Typography variant='body1' paragraph>
                Clutter Map is built with a modern and robust tech stack that supports scalability, security,
                and a seamless user experience:
            </Typography>

            <Box >
                <Typography variant="h6" align='left'>
                    Infrastructure and Deployment
                </Typography>
                <List sx={{ pt: 0 }}>
                    <ListItem>
                        <ListItemText
                            primary="AWS"
                            secondary="EC2 (Elastic Compute Cloud), RDS (Relational Database Service), CodeDeploy"
                        />
                    </ListItem>
                    <ListItem>
                        <ListItemText
                            primary="GitHub Actions"
                            secondary="Used for CI/CD pipelines, automating testing, building, and deployment 
                            processes to ensure code quality and streamline updates."
                        />
                    </ListItem>
                </List>
            </Box>

            <Box>
                <Typography variant="h6" gutterBottom align='left'>
                    Backend Development
                </Typography>
                <List sx={{ pt: 0 }}>
                    <ListItem>
                        <ListItemText
                            primary="Java Spring"
                            secondary="Provides a robust, secure framework for building RESTful APIs. Includes 
                            Spring Security with JWT authentication to ensure that users can only access their own data.
                            Uses Spring Data JPA for seamless interaction with the PostgreSQL database."
                        />
                    </ListItem>
                </List>
            </Box>

            <Box>
                <Typography variant="h6" gutterBottom align='left'>
                    Authentication
                </Typography>
                <List sx={{ pt: 0 }}>
                    <ListItem>
                        <ListItemText
                            primary="Google OAuth"
                            secondary="Handles user authentication, simplifying the login process and leveraging Google’s security measures for user data protection."
                        />
                    </ListItem>
                </List>
            </Box>

            <Box>
                <Typography variant="h6" gutterBottom align='left'>
                    Frontend Development
                </Typography>
                <List sx={{ pt: 0 }}>
                    <ListItem>
                        <ListItemText
                            primary="React"
                            secondary="Enables a dynamic, component-based UI that is responsive and easy to maintain."
                        />
                    </ListItem>
                    <ListItem>
                        <ListItemText
                            primary="TypeScript"
                            secondary="Adds static typing to JavaScript, improving code quality by catching errors 
                            early and making the codebase more maintainable."
                        />
                    </ListItem>
                    <ListItem>
                        <ListItemText
                            primary="Redux RTK (Redux Toolkit)"
                            secondary="Centralizes the application’s state, ensuring consistent data access across 
                            components and improving overall data management."
                        />
                    </ListItem>
                </List>
            </Box>

            <Box>
                <Typography variant="h6" gutterBottom align='left'>
                    Database
                </Typography>
                <List sx={{ pt: 0 }}>
                    <ListItem>
                        <ListItemText
                            primary="PostgreSQL"
                            secondary="Reliable relational database supporting complex data relationships, ideal for tracking items and locations in Clutter Map."
                        />
                    </ListItem>
                </List>
            </Box>
        </Container>
    )
}

export default AboutPage