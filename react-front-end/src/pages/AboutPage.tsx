import React from 'react';

import { Box, List, ListItem, ListItemText, ListSubheader, Typography } from '@mui/material';

const AboutPage: React.FC = () => {
    return (
        <Box
            maxWidth="md"
            sx={{
                gap: 3,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
            }}>
            <Typography variant='h3'>
                About Clutter Map
            </Typography>

            <Typography variant='h4' gutterBottom sx={{ mt: 3 }}>
                What is Clutter Map?
            </Typography>
            <Typography variant='body1' paragraph align='left'>
                Clutter Map is an innovative web application designed to help users
                keep track of their belongings. It provides a system for knowing exactly
                where everything belongs, making it easier to put items away or find them
                later. Clutter Map has a wide range of potential uses, from personal
                organization to enhancing the guest experience in short-term rentals by ensuring
                everything is in its designated place.
            </Typography>

            <Typography variant='h4' gutterBottom sx={{ mt: 6 }}>
                Why We Created Clutter Map
            </Typography>
            <Typography variant='body1' paragraph align='left'>
                We built Clutter Map to address a common issue: people often don’t know where
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
            {/* <Typography variant="body1" paragraph>
                In addition to solving these everyday problems, Clutter Map has been a valuable project
                for us to deepen our skills, explore new technologies, and build a genuinely useful application.
            </Typography> */}

            <Typography variant='h4' gutterBottom sx={{ mt: 6 }}>
                About the team
            </Typography>

            <Typography variant='h6' align="left" gutterBottom>
                Rebecca Gilbert-Croysdale
            </Typography>
            <Typography variant='body1' paragraph align="left">
                A senior full stack engineer, she leads the Clutter Map project, bringing expertise
                in modern web technologies and software architecture. Rebecca is a seasoned full stack engineer with a strong
                background in web application development, architecture, and design.
            </Typography>
            <Typography variant='body1' paragraph align="left">
                With a history of driving products from ideation to global adoption,
                she has experience coordinating across departments to launch impactful platforms. Notable past
                achievements include developing a conversational recruiting platform with a role-specific scoring
                framework and reducing new job posting setup times by leveraging algorithms and natural language processing.
            </Typography>
            {/* <Typography variant='body1' paragraph align="left">
                Rebecca has a well-rounded skill set that spans frontend, backend, and DevOps practices, including 
                CI/CD pipelines and Docker containerization. Rebecca is committed to delivering accessible and 
                performant user interfaces.
            </Typography> */}

            <Typography variant='h6' align="left" gutterBottom>
                Alanna Croysdale
            </Typography>
            <Typography variant='body1' paragraph align="left">
                Alanna joined as a junior engineer after recently graduating with a Computer Science degree focused on Security.
                Eager to gain hands-on experience with a larger codebase, Alanna is building technical proficiency and learning
                best practices through this collaboration. Meanwhile, Rebecca is developing mentoring skills, creating a mutually
                beneficial learning experience within the Clutter Map project.
            </Typography>

            <Typography variant='h4' gutterBottom sx={{ mt: 6 }}>
                Technologies
            </Typography>
            <Typography variant='body1' paragraph align='left'>
                Clutter Map is built with a modern and robust tech stack that supports scalability, security,
                and a seamless user experience:
            </Typography>
            <List>

                <ListSubheader component="div" sx={{ bgcolor: 'transparent', pl: 0 }}>
                    <Typography variant="h6" align='left'>
                        Infrastructure and Deployment
                    </Typography>
                </ListSubheader>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="AWS"
                        secondary="EC2 (Elastic Compute Cloud), RDS (Relational Database Service), CodeDeploy"
                    />
                </ListItem>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="GitHub Actions"
                        secondary="Used for CI/CD pipelines, automating testing, building, and deployment 
                            processes to ensure code quality and streamline updates."
                    />
                </ListItem>

                <ListSubheader component="div" sx={{ bgcolor: 'transparent', pl: 0 }}>
                    <Typography variant="h6" align='left'>
                        Backend Development
                    </Typography>
                </ListSubheader>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="Java Spring"
                        secondary="Provides a robust, secure framework for building RESTful APIs. Includes 
                            Spring Security with JWT authentication to ensure that users can only access their own data.
                            Uses Spring Data JPA for seamless interaction with the PostgreSQL database."
                    />
                </ListItem>

                <ListSubheader component="div" sx={{ bgcolor: 'transparent', pl: 0 }}>
                    <Typography variant="h6" gutterBottom align='left'>
                        Authentication
                    </Typography>
                </ListSubheader>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="Google OAuth"
                        secondary="Handles user authentication, simplifying the login process and leveraging Google’s 
                            security measures for user data protection."
                    />
                </ListItem>

                <ListSubheader component="div" sx={{ bgcolor: 'transparent', pl: 0 }}>
                    <Typography variant="h6" gutterBottom align='left'>
                        Frontend Development
                    </Typography>
                </ListSubheader>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="React"
                        secondary="Enables a dynamic, component-based UI that is responsive and easy to maintain."
                    />
                </ListItem>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="TypeScript"
                        secondary="Adds static typing to JavaScript, improving code quality by catching errors 
                            early and making the codebase more maintainable."
                    />
                </ListItem>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="Redux RTK (Redux Toolkit)"
                        secondary="Centralizes the application’s state, ensuring consistent data access across 
                            components and improving overall data management."
                    />
                </ListItem>
                <ListItem sx={{ py: 0 }}>
                    <ListItemText
                        primary="Material UI"
                        secondary="Provides a set of customizable components that align with modern design standards, allowing for a consistent, accessible, and visually appealing user interface."
                    />
                </ListItem>

                <ListSubheader component="div" sx={{ bgcolor: 'transparent', pl: 0 }}>
                    <Typography variant="h6" gutterBottom align='left'>
                        Database
                    </Typography>
                </ListSubheader>
                <ListItem sx={{ pt: 0, pb: 15 }}>
                    <ListItemText
                        primary="PostgreSQL"
                        secondary="Reliable relational database supporting complex data relationships, ideal for 
                            tracking items and locations in Clutter Map."
                    />
                </ListItem>
            </List>
        </Box>
    )
}

export default AboutPage