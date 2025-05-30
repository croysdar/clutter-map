## Frontend Overview

The frontend of Clutter Map is built with React, TypeScript, and Vite. It supports modern development features like hot module replacement (HMR), PWA capabilities with offline support, and modular alias-based imports. Jest and Testing Library are used for testing, with separate configurations for browser and Node environments.

## Available Scripts

In the project directory (`react-front-end`), you can run:

### `npm run dev`

Runs the app in the development mode.\
Open [http://localhost:5173](http://localhost:5173) to view it in the browser.

The page will reload if you make edits.\
You will also see any lint errors in the console.

### `npm run build`

Builds the app for production to the dist/ directory.

### `npm run preview`

Locally preview the production build.

### `npm test`

Runs browser-related tests (JSDOM environment) using Jest and Testing Library.\

### `npm test:node`

Runs backend-related or node-only tests using a pure Node environment.

### `npm test:all`

Runs both test suites in sequence.

## Path Aliases

You can use the `@/` alias to simplify imports from the `src/` directory. For example:

```ts
import { client } from "@/services/client";
import theme from "@/contexts/theme";
```
