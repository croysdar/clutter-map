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
        '<rootDir>/jest.node.setup.js',
    ],
    testMatch: ["**/tests/*.test.ts", "**/tests/*.test.tsx"],
};

module.exports = config;