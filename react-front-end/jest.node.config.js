/** @type {import('jest').Config} */
const config = {
    preset: 'ts-jest',
    testEnvironment: 'node',
    moduleNameMapper: {
        "^@/(.*)$": "<rootDir>/src/$1",
    },
    transform: {
        "^.+\\.tsx?$": "ts-jest"
    },
    setupFiles: [
        'jest-localstorage-mock',
        'fake-indexeddb/auto'
    ],
    testMatch: ["**/*.test.ts", "**/*.test.tsx"],
};

module.exports = config;